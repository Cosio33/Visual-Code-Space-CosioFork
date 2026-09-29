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

package com.teixeira.vcspace.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ResetTv
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ToastUtils
import com.teixeira.vcspace.core.ai.AiManager
import com.teixeira.vcspace.core.ai.AiSettings
import com.teixeira.vcspace.resources.R
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.sliderPreference
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference

@Composable
fun AiSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val aiSettings = remember { AiSettings(context) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Read initial values
    val apiKey = remember { mutableStateOf(aiSettings.apiKey) }
    val modelName = remember { mutableStateOf(aiSettings.modelName) }
    val temperature = remember { mutableStateOf(aiSettings.temperature) }
    val useCustomConfig = remember { mutableStateOf(aiSettings.useCustomConfig) }
    val promptExplainCode = remember { mutableStateOf(aiSettings.promptExplainCode) }
    val promptGenerateCode = remember { mutableStateOf(aiSettings.promptGenerateCode) }
    val promptImportComponents = remember { mutableStateOf(aiSettings.promptImportComponents) }
    val promptCompleteCode = remember { mutableStateOf(aiSettings.promptCompleteCode) }
    val promptEditCode = remember { mutableStateOf(aiSettings.promptEditCode) }

    BackHandler(onBack = onNavigateUp)

    val backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset AI Settings") },
            text = { Text(stringResource(R.string.ai_reset_defaults_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    aiSettings.modelName = AiSettings.DEFAULT_MODEL
                    aiSettings.temperature = 0.7f
                    aiSettings.promptExplainCode = AiSettings.DEFAULT_PROMPT_EXPLAIN_CODE
                    aiSettings.promptGenerateCode = AiSettings.DEFAULT_PROMPT_GENERATE_CODE
                    aiSettings.promptImportComponents = AiSettings.DEFAULT_PROMPT_IMPORT_COMPONENTS
                    aiSettings.promptCompleteCode = AiSettings.DEFAULT_PROMPT_COMPLETE_CODE
                    aiSettings.promptEditCode = AiSettings.DEFAULT_PROMPT_EDIT_CODE

                    modelName.value = aiSettings.modelName
                    temperature.value = aiSettings.temperature
                    promptExplainCode.value = aiSettings.promptExplainCode
                    promptGenerateCode.value = aiSettings.promptGenerateCode
                    promptImportComponents.value = aiSettings.promptImportComponents
                    promptCompleteCode.value = aiSettings.promptCompleteCode
                    promptEditCode.value = aiSettings.promptEditCode

                    AiManager.resetProvider()
                    showResetDialog = false
                    ToastUtils.showShort(context.getString(R.string.ai_settings_reset))
                }) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        preferenceCategory(
            key = "ai_api_category",
            title = { Text("API Configuration") }
        )

        switchPreference(
            key = "ai_use_custom_config",
            title = { Text(stringResource(R.string.ai_custom_config)) },
            summary = { Text(stringResource(R.string.ai_custom_config_summary)) },
            rememberState = { useCustomConfig },
            defaultValue = useCustomConfig.value,
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Top)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_api_key",
            title = { Text(stringResource(R.string.ai_api_key)) },
            summary = {
                Text(
                    if (apiKey.value.isNotEmpty()) "••••••••" + apiKey.value.takeLast(4)
                    else stringResource(R.string.ai_api_key_summary)
                )
            },
            rememberState = { apiKey },
            defaultValue = apiKey.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Key, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_model_name",
            title = { Text(stringResource(R.string.ai_model_name)) },
            summary = { Text(stringResource(R.string.ai_model_name_summary)) },
            rememberState = { modelName },
            defaultValue = modelName.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        sliderPreference(
            key = "ai_temperature",
            title = { Text(stringResource(R.string.ai_temperature)) },
            defaultValue = temperature.value,
            rememberState = { temperature },
            valueRange = 0.0f..2.0f,
            valueSteps = 20,
            enabled = { useCustomConfig.value },
            valueText = { Text(stringResource(R.string.ai_temperature_summary, it)) },
            icon = {
                Icon(imageVector = Icons.Default.Terminal, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Bottom)
                .background(backgroundColor)
        )

        preferenceCategory(
            key = "ai_prompts_category",
            title = { Text("System Prompts") }
        )

        textFieldPreference(
            key = "ai_prompt_explain_code",
            title = { Text(stringResource(R.string.ai_prompt_explain_code)) },
            summary = { Text(it.take(50) + if (it.length > 50) "..." else "") },
            rememberState = { promptExplainCode },
            defaultValue = promptExplainCode.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Top)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_prompt_generate_code",
            title = { Text(stringResource(R.string.ai_prompt_generate_code)) },
            summary = { Text(it.take(50) + if (it.length > 50) "..." else "") },
            rememberState = { promptGenerateCode },
            defaultValue = promptGenerateCode.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_prompt_import_components",
            title = { Text(stringResource(R.string.ai_prompt_import_components)) },
            summary = { Text(it.take(50) + if (it.length > 50) "..." else "") },
            rememberState = { promptImportComponents },
            defaultValue = promptImportComponents.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_prompt_complete_code",
            title = { Text(stringResource(R.string.ai_prompt_complete_code)) },
            summary = { Text(it.take(50) + if (it.length > 50) "..." else "") },
            rememberState = { promptCompleteCode },
            defaultValue = promptCompleteCode.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        textFieldPreference(
            key = "ai_prompt_edit_code",
            title = { Text(stringResource(R.string.ai_prompt_edit_code)) },
            summary = { Text(it.take(50) + if (it.length > 50) "..." else "") },
            rememberState = { promptEditCode },
            defaultValue = promptEditCode.value,
            textToValue = { it },
            enabled = { useCustomConfig.value },
            icon = {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Middle)
                .background(backgroundColor)
        )

        preference(
            key = "ai_reset_defaults",
            title = { Text(stringResource(R.string.ai_reset_defaults)) },
            onClick = { showResetDialog = true },
            icon = {
                Icon(imageVector = Icons.Default.ResetTv, contentDescription = null)
            },
            modifier = Modifier
                .clip(PreferenceShape.Bottom)
                .background(backgroundColor)
        )
    }

    // Save values when they change
    LaunchedEffect(apiKey.value) { aiSettings.apiKey = apiKey.value }
    LaunchedEffect(modelName.value) {
        aiSettings.modelName = modelName.value
        AiManager.resetProvider()
    }
    LaunchedEffect(temperature.value) {
        aiSettings.temperature = temperature.value
        AiManager.resetProvider()
    }
    LaunchedEffect(useCustomConfig.value) {
        aiSettings.useCustomConfig = useCustomConfig.value
        AiManager.resetProvider()
    }
    LaunchedEffect(promptExplainCode.value) { aiSettings.promptExplainCode = promptExplainCode.value }
    LaunchedEffect(promptGenerateCode.value) { aiSettings.promptGenerateCode = promptGenerateCode.value }
    LaunchedEffect(promptImportComponents.value) { aiSettings.promptImportComponents = promptImportComponents.value }
    LaunchedEffect(promptCompleteCode.value) { aiSettings.promptCompleteCode = promptCompleteCode.value }
    LaunchedEffect(promptEditCode.value) { aiSettings.promptEditCode = promptEditCode.value }
}
