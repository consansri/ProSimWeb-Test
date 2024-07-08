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

    fun delete(selection: Selection) {
        val start = selection.start ?: return
        val end = selection.end ?: return
        selection.deselect()
        delete(start, end)
    }

    fun replaceAll(new: String)
}