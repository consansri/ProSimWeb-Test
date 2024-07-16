package cengine.editor.text.indentation

import cengine.editor.selection.Selector

interface IndentationProvider {

    val spaces: Int

    /**
     * @return number of spaces which where inserted.
     */
    fun indentAtIndex(index: Int): Int

    /**
     * @return number of spaces which where removed.
     */
    fun unIndentAtIndex(index: Int): Int
    fun indentSelection(selector: Selector) {
        val range = selector.selection.asRange()
        if (range != null) {
            // Index Indentation
            selector.caret += indentAtIndex(selector.caret.index)
        } else {
            // Selection Indentation

        }
    }

    fun unindentSelection(selector: Selector) {
        val range = selector.selection.asRange()
        if (range != null) {
            // Index Unindent
            val length = indentAtIndex(selector.caret.index)
            selector.caret.set(selector.caret.line, (selector.caret.col - length).coerceAtLeast(0))
        } else {
            // Selection Unindent

        }

    }
}