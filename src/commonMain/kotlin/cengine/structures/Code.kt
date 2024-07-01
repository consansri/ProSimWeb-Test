package cengine.structures

interface Code {

    val length: Int

    fun insert(index: Int, new: String)

    fun delete(start: Int, end: Int)

    fun substring(start: Int, end: Int): String

    fun charAt(index: Int): Char

    fun getLineAndColumn(index: Int): Pair<Int, Int>

    fun getIndexFromLineAndColumn(line: Int, column: Int): Int

    override fun toString(): String



}