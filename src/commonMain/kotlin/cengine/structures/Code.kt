package cengine.structures

interface Code {

    val length: Int

    fun insert(index: Int, new: String)

    fun delete(start: Int, end: Int)

    fun substring(start: Int, end: Int): String

    fun charAt(index: Int): Char

    override fun toString(): String

}