/*
 * This file is part of Visual Code Space.
 *
 * Visual Code Space is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Visual Code Space is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Visual Code Space.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira.vcspace.core.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.teixeira.vcspace.core.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiProvider(private val settings: AiSettings) : AiProvider {

    private fun createModel(): GenerativeModel {
        val apiKey = getEffectiveApiKey()
        return GenerativeModel(
            modelName = settings.modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = settings.temperature
                topK = 64
                topP = 0.95f
                maxOutputTokens = 65536
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            )
        )
    }

    private fun getEffectiveApiKey(): String {
        val customKey = settings.apiKey
        return if (customKey.isNotBlank() && settings.useCustomConfig) {
            customKey
        } else {
            Secrets.getGenerativeAiApiKey()
        }
    }

    private suspend fun generateContent(prompt: String) = withContext(Dispatchers.IO) {
        runCatching {
            createModel().generateContent(
                content { text(prompt) }
            )
        }
    }

    private fun parseResponse(response: com.google.ai.client.generativeai.type.GenerateContentResponse): AiResponse {
        val text = response.candidates.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.asTextOrNull()
            ?: ""

        val usage = response.usageMetadata
        return AiResponse(
            text = text,
            totalTokenCount = usage?.totalTokenCount,
            promptTokenCount = usage?.promptTokenCount,
            candidatesTokenCount = usage?.candidatesTokenCount
        )
    }

    override suspend fun explainCode(code: String): Result<AiResponse> {
        val prompt = if (settings.useCustomConfig) {
            settings.promptExplainCode.replace("%code%", code)
        } else {
            "Explain the following code in simple terms:\n\n$code\n"
        }
        return generateContent(prompt).map { parseResponse(it) }
    }

    override suspend fun importComponents(code: String): Result<AiResponse> {
        if (!isJetpackComposeCode(code)) {
            return Result.failure(IllegalArgumentException("The provided code does not appear to be Jetpack Compose code."))
        }

        val prompt = if (settings.useCustomConfig) {
            settings.promptImportComponents.replace("%code%", code)
        } else {
            "Provide the necessary Jetpack Compose imports for the following code snippet, ensuring all required components are included (only provide code):\n\n$code\n"
        }
        return generateContent(prompt).map { parseResponse(it) }
    }

    override suspend fun generateCode(prompt: String, fileExtension: String?): Result<AiResponse> {
        val finalPrompt = if (settings.useCustomConfig) {
            settings.promptGenerateCode
                .replace("%prompt%", prompt)
                .replace("%extension%", if (!fileExtension.isNullOrEmpty()) " for file extension $fileExtension" else "")
        } else {
            "Write the code based on my prompt${if (!fileExtension.isNullOrEmpty()) " for file extension $fileExtension" else ""} and provide me only code:\nThe prompt:\n\n$prompt"
        }
        return generateContent(finalPrompt).map { parseResponse(it) }
    }

    override suspend fun editCode(code: String, instructions: String, language: String): Result<AiResponse> {
        val finalPrompt = if (settings.useCustomConfig) {
            settings.promptEditCode
                .replace("%code%", code)
                .replace("%instructions%", instructions)
                .replace("%language%", language.ifEmpty { "code" })
        } else {
            """
      Modify the following ${language.ifEmpty { "code" }} code according to these instructions:
      
      Instructions: $instructions
      
      Code to modify:
      $code
      
      Please provide only the modified code without additional comments or explanations.
    """.trimIndent()
        }
        return generateContent(finalPrompt).map { parseResponse(it) }
    }

    override suspend fun completeCode(completionMetadata: CompletionMetadata): Result<AiResponse> {
        val finalPrompt = if (settings.useCustomConfig) {
            settings.promptCompleteCode
                .replace("%language%", completionMetadata.language)
                .replace("%before%", completionMetadata.textBeforeCursor)
                .replace("%after%", completionMetadata.textAfterCursor)
        } else {
            """
      Please complete the following ${completionMetadata.language} code:
      
      ${completionMetadata.textBeforeCursor}
      <cursor>
      ${completionMetadata.textAfterCursor}
      
      Use modern ${completionMetadata.language} practices and hooks where appropriate. Please provide only the completed part of the
      code without additional comments or explanations.
    """.trimIndent()
        }
        return generateContent(finalPrompt).map { parseResponse(it) }
    }

    companion object {
        fun removeBackticksFromMarkdownCodeBlock(codeWithBackticks: String?): String {
            codeWithBackticks ?: return ""

            val trimmedCode = codeWithBackticks.trim()

            if (trimmedCode.startsWith("```") && trimmedCode.endsWith("```")) {
                val firstNewlineIndex = trimmedCode.indexOf("\n")
                return if (firstNewlineIndex > 3) {
                    trimmedCode.substring(firstNewlineIndex + 1, trimmedCode.length - 3).trim()
                } else {
                    trimmedCode.substring(3, trimmedCode.length - 3).trim()
                }
            }

            return codeWithBackticks
        }

        private fun isJetpackComposeCode(code: String): Boolean {
            val composeKeywords = listOf(
                "@Composable", "Modifier", "Column", "Row", "Button", "Text", "Box",
                "LazyColumn", "LazyRow", "remember", "mutableStateOf"
            )
            return composeKeywords.any { keyword -> code.contains(keyword) }
        }
    }
}
