package cengine.editor.text.indentation

import cengine.editor.selection.Selection
import cengine.editor.text.Editable
import cengine.editor.text.Informational

class BasicIndenation(private val editable: Editable, private val informational: Informational, override val spaces: Int = 4) : IndentationProvider {

    override fun indentAtIndex(index: Int): Int {
        val (line, column) = informational.getLineAndColumn(index)
        val location = column % spaces
        val spacesToIndent = spaces - location
        editable.insert(index, " ".repeat(spacesToIndent))
        return spacesToIndent
    }

    override fun unIndentAtIndex(index: Int): Int {
        val (line, column) = informational.getLineAndColumn(index)
        val lineStartIndex = informational.indexOf(line, 0)
        for (length in spaces downTo 1) {
            if (informational.substring(lineStartIndex, lineStartIndex + length) == " ".repeat(length)) {
                editable.delete(lineStartIndex, lineStartIndex + length)
                return length
            }
        }
        return 0
    }
}