package cengine.editor.completion

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.editor.annotation.Severity

data class Completion(val text: String, val type: String, val insertion: String) : EditorModification {
    override val severity: Severity? = null
    override val displayText: String get() = text
    override val execute: (CodeEditor) -> Unit = {
        it.textStateModel.insert(it.selector.caret, insertion)
    }
}
