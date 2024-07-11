package cengine.editor.selection

interface Selector {

    val caret: Caret
    val selection: Selection

    // Modification

    fun moveCaretTo(index: Int, shift: Boolean) {
        if (shift) {
            if (!selection.valid()) {
                selection.select(caret.index, index)
            } else {
                selection.moveEnd(index)
            }
        } else {
            selection.deselect()
        }
        caret.set(index)
    }

    fun moveCaretTo(line: Int, column: Int, shift: Boolean) {
        val index = caret.model.getIndexFromLineAndColumn(line, column)
        moveCaretTo(index, shift)
    }

    fun moveCaretLeft(offset: Int, shift: Boolean) {
        val range = selection.asRange()
        val newIndex = if (!shift && range != null && range.first != caret.index) {
            // move the caret to the beginning of the selection if it isn't already there.
            range.first
        } else {
            (caret.index - offset).coerceAtLeast(0)
        }

        moveCaretTo(newIndex, shift)
    }

    fun moveCaretRight(offset: Int, shift: Boolean) {
        val range = selection.asRange()
        val newIndex = if (!shift && range != null && range.last + 1 != caret.index) {
            // move the caret to the end of the selection if it isn't already there.
            range.last + 1
        } else {
            (caret.index + offset).coerceAtMost(caret.model.length)
        }

        moveCaretTo(newIndex, shift)
    }

    fun moveCaretUp(offset: Int, shift: Boolean) {
        val range = selection.asRange()

        val newLine = if (!shift && range != null && range.first != caret.index) {
            // move the caret above the current selection if it isn't already at the lower bound of the selection.
            (caret.model.getLineAndColumn(range.first).first - 1).coerceAtLeast(0)
        } else {
            (caret.line - offset).coerceAtLeast(0)
        }
        val newIndex = caret.model.getIndexFromLineAndColumn(newLine, caret.col)
        moveCaretTo(newIndex, shift)
    }

    fun moveCaretDown(offset: Int, shift: Boolean) {
        val range = selection.asRange()

        val newLine = if (!shift && range != null && range.last + 1 != caret.index) {
            // move the caret under the current selection if it isn't already at the higher bound of the selection.
            (caret.model.getLineAndColumn(range.last + 1).first + 1).coerceAtMost(caret.model.lines)
        } else {
            (caret.line + offset).coerceAtMost(caret.model.lines)
        }
        val newIndex = caret.model.getIndexFromLineAndColumn(newLine, caret.col)
        moveCaretTo(newIndex, shift)
    }

    fun home(shift: Boolean) {
        moveCaretTo(caret.line, 0, shift)
    }

    fun end(shift: Boolean) {
        moveCaretTo(caret.line, Int.MAX_VALUE, shift)
    }

    // Information

    fun caretIsAtHigherBoundOfSel(): Boolean = caret.index == selection.higher

    fun caretIsAtLowerBoundsOfSel(): Boolean = caret.index == selection.lower


}