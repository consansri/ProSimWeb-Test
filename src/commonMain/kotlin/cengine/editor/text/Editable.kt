package cengine.editor.text

import cengine.editor.selection.Caret
import cengine.editor.selection.Selection

interface Editable {
    fun insert(index: Int, new: String)

    fun insert(caret: Caret, new: String) {
        insert(caret.index, new)
        caret.index += new.length
    }

    fun delete(start: Int, end: Int)

    fun delete(selection: Selection): Int {
        val range = selection.asRange() ?: return 0
        selection.deselect()
        delete(range.first, range.last + 1)
        return range.count()
    }

    fun replaceAll(new: String)
}