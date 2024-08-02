package cengine.editor.annotation

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.psi.core.Locatable

data class Notation(val range: IntRange, val message: String, override val severity: Severity, override val execute: (CodeEditor) -> Unit = {}) : EditorModification {

    companion object {
        fun error(element: Locatable, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.textRange.toIntRange(), message, Severity.ERROR, execute)
        fun warn(element: Locatable, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.textRange.toIntRange(), message, Severity.WARNING, execute)
        fun info(element: Locatable, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.textRange.toIntRange(), message, Severity.INFO, execute)
    }

    override val displayText: String
        get() = message
}
