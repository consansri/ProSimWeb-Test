package cengine.editor.completion

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.editor.annotation.Severity

data class Completion(val text: String, val type: String,  override val severity: Severity?, override val execute: (CodeEditor) -> Unit): EditorModification{
    override val displayText: String get() = text
}
