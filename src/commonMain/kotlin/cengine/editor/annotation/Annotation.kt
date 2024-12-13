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

    fun location(file: PsiFile): String {
        val (line, column) = file.content.lineAndColumn(range.first)
        return "${line + 1}:${column + 1}"
    }

    fun createConsoleMessage(file: PsiFile): String {
        return "${file.file.path}:${location(file)} $message"
    }

    override val displayText: String get() = message
}
