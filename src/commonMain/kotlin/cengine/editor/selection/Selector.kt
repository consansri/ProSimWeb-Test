package cengine.editor.selection

interface Selector {

    val caret: Caret
    val selection: Selection

    companion object {
        val DEFAULT_SYMBOL_CHARS = ('a'.rangeTo('z') + 'A'.rangeTo('Z') + '0'.rangeTo('9') + '_').toCharArray()
        val DEFAULT_SPACING_SET = charArrayOf(' ', '\n')
        val ONLY_SPACES = charArrayOf(' ')
    }


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
        val index = caret.model.indexOf(line, column)
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
        val newIndex = caret.model.indexOf(newLine, caret.col)
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
        val newIndex = caret.model.indexOf(newLine, caret.col)
        moveCaretTo(newIndex, shift)
    }

    fun home(shift: Boolean) {
        val rowStartIndex = caret.model.indexOf(caret.line,0)
        val indexOfFirstValidInCol = indexOfWordEnd(rowStartIndex, ONLY_SPACES, true)
        if (caret.index != indexOfFirstValidInCol) {
            moveCaretTo(indexOfFirstValidInCol, shift)
        } else {
            moveCaretTo(rowStartIndex, shift)
        }
    }

    fun end(shift: Boolean) {
        moveCaretTo(caret.line, Int.MAX_VALUE, shift)
    }

    // Information

    fun caretIsAtHigherBoundOfSel(): Boolean = caret.index == selection.higher

    fun caretIsAtLowerBoundsOfSel(): Boolean = caret.index == selection.lower

    /**
     * Select the word at the caret.
     */
    fun selectCurrentWord(index: Int, chars: CharArray, isValidSet: Boolean) {
        val start = indexOfWordStart(index, chars, isValidSet)
        val end = indexOfWordEnd(index, chars, isValidSet)
        selection.select(start, end)
        caret.set(end)
    }

    /**
     * Returns the index of the start of the current word that the caret is in.
     * If the caret is at a word boundary, it returns the start of the next word.
     * @param validChars Array of characters that are allowed to be part of the result.
     */
    fun indexOfWordStart(beginningIndex: Int, chars: CharArray, isValidSet: Boolean): Int {
        var index = beginningIndex

        // Move backward until we find an invalid character or the start of the text
        while (index > 0 && (caret.model.charAt(index - 1) in chars) == isValidSet) {
            index--
        }

        return index
    }

    /**
     * Returns the index of the end of the current word that the caret is in.
     * If the caret is at a word boundary, it returns the end of the previous word.
     * @param validChars Array of characters considered as part of a word
     */
    fun indexOfWordEnd(beginningIndex: Int, chars: CharArray, isValidSet: Boolean): Int {
        var index = beginningIndex

        // Move forward until we find an invalid character or the end of the text
        while (index < caret.model.length && (caret.model.charAt(index) in chars) == isValidSet) {
            index++
        }

        return index
    }


}