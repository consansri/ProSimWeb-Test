package cengine.text

interface TextModel {

    val length: Int
    val lines: Int

    fun insert(index: Int, new: String)

    fun delete(start: Int, end: Int)

    fun replaceAll(new: String)

    fun substring(start: Int, end: Int): String

    fun charAt(index: Int): Char

    fun getLineAndColumn(index: Int): Pair<Int, Int>

    fun getIndexFromLineAndColumn(line: Int, column: Int): Int

    override fun toString(): String
}