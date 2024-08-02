package cengine.psi.core

/**
 * Represents a range of text in a file.
 *
 * @param startOffset inclusive
 * @param endOffset exclusive
 */
data class TextRange(val startOffset: TextPosition, val endOffset: TextPosition) {
    constructor(start: Int, end: Int): this(TextPosition(start), TextPosition(end))
    operator fun contains(other: TextPosition): Boolean = other < endOffset && other >= startOffset
    operator fun contains(other: Int): Boolean = other < endOffset.index && other >= startOffset.index
    fun move(offset: TextPosition): TextRange = TextRange(startOffset + offset, endOffset + offset)
    fun difference(other: TextRange): TextPosition = TextPosition(endOffset.index - startOffset.index)
    fun toIntRange(): IntRange = IntRange(startOffset.index, endOffset.index - 1)
}
