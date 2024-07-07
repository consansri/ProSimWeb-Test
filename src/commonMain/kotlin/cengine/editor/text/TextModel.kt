package cengine.editor.text

import cengine.editor.selection.Caret
import cengine.editor.selection.Selection

interface TextModel {

    val length: Int
    val lines: Int

    fun insert(index: Int, new: String)

    fun insert(caret: Caret, new: String){
        insert(caret.index, new)
    }

    fun delete(start: Int, end: Int)

    fun delete(selection: Selection) {
        val start = selection.start ?: return
        val end = selection.end ?: return
        delete(start, end)
    }

    fun replaceAll(new: String)

    fun substring(start: Int, end: Int): String

    fun substring(selection: Selection): String{
        val start = selection.start ?: return ""
        val end = selection.end ?: return ""
        return substring(start, end)
    }

    fun charAt(index: Int): Char

    fun getLineAndColumn(index: Int): Pair<Int, Int>

    fun getIndexFromLineAndColumn(line: Int, column: Int): Int

    override fun toString(): String
}