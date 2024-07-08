package cengine.editor.selection

interface Selector {

    val caret: Caret
    val selection: Selection

    fun moveCaretTo(index: Int, shift: Boolean) {
        if (shift) {
            if(!selection.valid()){
                selection.select(caret.index, index)
            }else{
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
        val newIndex = (caret.index - offset).coerceAtLeast(0)
        moveCaretTo(newIndex, shift)
    }

    fun moveCaretRight(offset: Int, shift: Boolean) {
        val newIndex = (caret.index + offset).coerceAtMost(caret.model.length)
        moveCaretTo(newIndex, shift)
    }

    fun moveCaretUp(offset: Int, shift: Boolean) {
        val newLine = (caret.line - offset).coerceAtLeast(0)
        val newIndex = caret.model.getIndexFromLineAndColumn(newLine,caret.col)
        moveCaretTo(newIndex, shift)
    }

    fun moveCaretDown(offset: Int, shift: Boolean) {
        val newLine = (caret.line + offset).coerceAtMost(caret.model.lines)
        val newIndex = caret.model.getIndexFromLineAndColumn(newLine, caret.col)
        moveCaretTo(newIndex, shift)
    }

}