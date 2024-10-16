package cengine.editor.annotation

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.psi.core.Interval
import cengine.psi.core.PsiFile
import cengine.util.string.lineAndColumn

data class Annotation(val range: IntRange, val message: String, override val severity: Severity, override val execute: (CodeEditor) -> Unit = {}) : EditorModification {

    companion object {
        fun error(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.ERROR, execute)
        fun warn(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.WARNING, execute)
        fun info(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.INFO, execute)
    }

    fun createConsoleMessage(file: PsiFile): String {
        val (line, column) = file.content.lineAndColumn(range.first)
        val location = "${line + 1}:${column + 1}"

        return "${file.file.path}:$location $message"
    }

    override val displayText: String get() = message
}
