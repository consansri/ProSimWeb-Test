package cengine.psi.core

/**
 * Represents a range of text in a file.
 *
 * @param startOffset inclusive
 * @param endOffset exclusive
 */
data class TextRange(val startOffset: TextPosition, val endOffset: TextPosition) {
    val length: Int get() = endOffset.index - startOffset.index
    constructor(start: Int, end: Int) : this(TextPosition(start), TextPosition(end))
    constructor(index: Int) : this(TextPosition(index), TextPosition(index + 1))

    operator fun contains(other: TextPosition): Boolean = other < endOffset && other >= startOffset
    operator fun contains(other: Int): Boolean = other < endOffset.index && other >= startOffset.index
    operator fun plus(other: Int): TextRange = TextRange(startOffset.index + other, endOffset.index + other)
    operator fun minus(other: Int): TextRange = TextRange(startOffset.index - other, endOffset.index - other)
    fun move(offset: TextPosition): TextRange = TextRange(startOffset + offset, endOffset + offset)
    fun difference(other: TextRange): TextPosition = TextPosition(endOffset.index - startOffset.index)
    fun toIntRange(): IntRange = IntRange(startOffset.index, endOffset.index - 1)
    override fun toString(): String = "$startOffset..<$endOffset"
}
