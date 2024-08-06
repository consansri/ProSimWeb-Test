package cengine.editor.annotation

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.psi.core.Interval

data class Notation(val range: IntRange, val message: String, override val severity: Severity, override val execute: (CodeEditor) -> Unit = {}) : EditorModification {

    companion object {
        fun error(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.range, message, Severity.ERROR, execute)
        fun warn(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.range, message, Severity.WARNING, execute)
        fun info(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Notation = Notation(element.range, message, Severity.INFO, execute)
    }

    override val displayText: String
        get() = message
}
