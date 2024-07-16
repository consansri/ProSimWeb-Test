package cengine.editor.annotation

import cengine.editor.CodeEditor
import cengine.editor.EditorModification

data class Annotation(val range: IntRange, val message: String, override val severity: Severity, override val execute: (CodeEditor) -> Unit = {}): EditorModification {
    override val displayText: String
        get() = message
}
