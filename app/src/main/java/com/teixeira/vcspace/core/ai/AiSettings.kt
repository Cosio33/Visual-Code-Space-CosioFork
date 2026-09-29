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

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AiSettings(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "ai_secure_prefs"
        private const val KEY_API_KEY = "ai_api_key"
        private const val KEY_MODEL_NAME = "ai_model_name"
        private const val KEY_TEMPERATURE = "ai_temperature"
        private const val KEY_USE_CUSTOM_CONFIG = "ai_use_custom_config"

        // System prompts
        private const val KEY_PROMPT_EXPLAIN_CODE = "ai_prompt_explain_code"
        private const val KEY_PROMPT_GENERATE_CODE = "ai_prompt_generate_code"
        private const val KEY_PROMPT_IMPORT_COMPONENTS = "ai_prompt_import_components"
        private const val KEY_PROMPT_COMPLETE_CODE = "ai_prompt_complete_code"
        private const val KEY_PROMPT_EDIT_CODE = "ai_prompt_edit_code"

        const val DEFAULT_MODEL = "gemini-2.0-flash"
        private const val DEFAULT_TEMPERATURE = 0.7f

        const val DEFAULT_PROMPT_EXPLAIN_CODE = "Explain the following code in simple terms:\n\n%code%\n"
        const val DEFAULT_PROMPT_GENERATE_CODE = "Write the code based on my prompt%extension% and provide me only code:\nThe prompt:\n\n%prompt%"
        const val DEFAULT_PROMPT_IMPORT_COMPONENTS = "Provide the necessary Jetpack Compose imports for the following code snippet, ensuring all required components are included (only provide code):\n\n%code%\n"
        const val DEFAULT_PROMPT_COMPLETE_CODE = "Please complete the following %language% code:\n\n%before%\n<cursor>\n%after%\n\nUse modern %language% practices and hooks where appropriate. Please provide only the completed part of the code without additional comments or explanations."
        const val DEFAULT_PROMPT_EDIT_CODE = "Modify the following %language% code according to these instructions:\n\nInstructions: %instructions%\n\nCode to modify:\n%code%\n\nPlease provide only the modified code without additional comments or explanations. Make sure to preserve the same functionality unless the instructions explicitly ask to change it."
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var modelName: String
        get() = prefs.getString(KEY_MODEL_NAME, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL_NAME, value).apply()

    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()

    var useCustomConfig: Boolean
        get() = prefs.getBoolean(KEY_USE_CUSTOM_CONFIG, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_CUSTOM_CONFIG, value).apply()

    var promptExplainCode: String
        get() = prefs.getString(KEY_PROMPT_EXPLAIN_CODE, DEFAULT_PROMPT_EXPLAIN_CODE) ?: DEFAULT_PROMPT_EXPLAIN_CODE
        set(value) = prefs.edit().putString(KEY_PROMPT_EXPLAIN_CODE, value).apply()

    var promptGenerateCode: String
        get() = prefs.getString(KEY_PROMPT_GENERATE_CODE, DEFAULT_PROMPT_GENERATE_CODE) ?: DEFAULT_PROMPT_GENERATE_CODE
        set(value) = prefs.edit().putString(KEY_PROMPT_GENERATE_CODE, value).apply()

    var promptImportComponents: String
        get() = prefs.getString(KEY_PROMPT_IMPORT_COMPONENTS, DEFAULT_PROMPT_IMPORT_COMPONENTS) ?: DEFAULT_PROMPT_IMPORT_COMPONENTS
        set(value) = prefs.edit().putString(KEY_PROMPT_IMPORT_COMPONENTS, value).apply()

    var promptCompleteCode: String
        get() = prefs.getString(KEY_PROMPT_COMPLETE_CODE, DEFAULT_PROMPT_COMPLETE_CODE) ?: DEFAULT_PROMPT_COMPLETE_CODE
        set(value) = prefs.edit().putString(KEY_PROMPT_COMPLETE_CODE, value).apply()

    var promptEditCode: String
        get() = prefs.getString(KEY_PROMPT_EDIT_CODE, DEFAULT_PROMPT_EDIT_CODE) ?: DEFAULT_PROMPT_EDIT_CODE
        set(value) = prefs.edit().putString(KEY_PROMPT_EDIT_CODE, value).apply()

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }
}
