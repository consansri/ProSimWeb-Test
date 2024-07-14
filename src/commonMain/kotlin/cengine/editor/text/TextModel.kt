package cengine.editor.text

import cengine.editor.selection.Selection

interface TextModel: Editable {

    val length: Int
    val lines: Int
    val maxColumns: Int

    fun substring(start: Int, end: Int): String

    fun substring(selection: Selection): String {
        val start = selection.start ?: return ""
        val end = selection.end ?: return ""
        return substring(start, end)
    }

    fun charAt(index: Int): Char

    /**
     * line: 0..lines
     *
     * column: 0...Int.MAX_VALUE
     *
     * @return line and column of a certain index.
     */
    fun getLineAndColumn(index: Int): Pair<Int, Int>

    /**
     * line: 0..lines
     *
     * column: 0...Int.MAX_VALUE
     *
     * @return line and column of a certain index.
     */
    fun indexOf(line: Int, column: Int): Int

    /**
     * Searches all occurences of [searchString] in the model and returns a list of [IntRange].
     */
    fun findAllOccurrences(searchString: String, ignoreCase: Boolean = false): List<IntRange>

    override fun toString(): String
}