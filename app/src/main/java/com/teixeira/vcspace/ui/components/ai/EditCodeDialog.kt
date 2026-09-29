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

package com.teixeira.vcspace.ui.components.ai

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ToastUtils
import com.itsvks.monaco.MonacoEditor
import com.teixeira.vcspace.app.strings
import com.teixeira.vcspace.core.ai.AiManager
import com.teixeira.vcspace.core.ai.GeminiProvider
import com.teixeira.vcspace.editor.VCSpaceEditor
import com.teixeira.vcspace.ui.screens.editor.components.view.CodeEditorView
import com.teixeira.vcspace.utils.launchWithProgressDialog
import io.github.rosemoe.sora.text.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EditCodeDialog(
    editor: View,
    selectedCode: CharSequence,
    fileExtension: String? = null,
    cursorLeftLine: Int = 0,
    cursorLeftColumn: Int = 0,
    cursorRightLine: Int = 0,
    cursorRightColumn: Int = 0,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var instructions by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(true) }
    var showResultSheet by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            title = {
                Text(text = stringResource(strings.edit_code))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(strings.edit_code_instructions),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text(stringResource(strings.edit_code_instructions_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 8
                    )

                    Text(
                        text = "Selected code (${selectedCode.length} chars):",
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = selectedCode.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        minLines = 3,
                        maxLines = 8
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDismiss()
                }) {
                    Text(stringResource(strings.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (instructions.isNotEmpty()) {
                            scope.launchWithProgressDialog(
                                uiContext = context,
                                configureBuilder = {
                                    it.apply {
                                        setMessage(strings.editing_code)
                                        setCancelable(false)
                                    }
                                }
                            ) { _, _ ->
                                val provider = AiManager.getProvider(context)
                                provider.editCode(
                                    code = selectedCode.toString(),
                                    instructions = instructions,
                                    language = fileExtension ?: ""
                                ).onSuccess { response ->
                                    val text = response.text
                                    resultText = text

                                    withContext(Dispatchers.Main) {
                                        showDialog = false
                                        showResultSheet = true
                                    }
                                }.onFailure {
                                    withContext(Dispatchers.Main) {
                                        ToastUtils.showShort(it.message)
                                    }
                                }
                            }
                        } else {
                            ToastUtils.showShort(context.getString(strings.enter_prompt))
                        }
                    }
                ) {
                    Text(stringResource(strings.generate))
                }
            }
        )
    }

    if (showResultSheet && resultText.isNotEmpty()) {
        EditCodeResultDialog(
            modifiedCode = resultText,
            onDismissRequest = {
                showResultSheet = false
                onDismiss()
            },
            onApply = { codeToApply ->
                val cleanCode = GeminiProvider.removeBackticksFromMarkdownCodeBlock(codeToApply)
                if (editor is MonacoEditor) {
                    // For MonacoEditor, replace selected text
                    val position = editor.position
                    editor.insert(
                        text = cleanCode,
                        position = position
                    )
                } else if (editor is CodeEditorView) {
                    val vcSpaceEditor = editor.editor as? VCSpaceEditor
                    if (vcSpaceEditor != null) {
                        val content: Content = vcSpaceEditor.text
                        // Use saved cursor positions (selection may have been cleared)
                        if (cursorLeftLine != cursorRightLine || cursorLeftColumn != cursorRightColumn) {
                            // Replace the original selection with modified code
                            content.delete(cursorLeftLine, cursorLeftColumn, cursorRightLine, cursorRightColumn)
                            content.insert(cursorLeftLine, cursorLeftColumn, cleanCode)
                            vcSpaceEditor.setSelection(cursorLeftLine, cursorLeftColumn + cleanCode.length)
                        } else {
                            // Fallback: insert at current cursor
                            val cursor = content.cursor
                            content.insert(cursor.leftLine, cursor.leftColumn, cleanCode)
                        }
                    }
                }
                showResultSheet = false
                onDismiss()
            }
        )
    }
}
