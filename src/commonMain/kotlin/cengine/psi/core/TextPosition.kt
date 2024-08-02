package cengine.psi.core

/**
 * Represents a position by line and column in a text.
 */
data class TextPosition(val index: Int = 0) : Comparable<TextPosition> {
    operator fun rangeUntil(end: TextPosition): TextRange = TextRange(this, end)
    operator fun plus(other: TextPosition): TextPosition = TextPosition(index + other.index)
    operator fun minus(other: TextPosition): TextPosition = TextPosition(index - other.index)
    override fun compareTo(other: TextPosition): Int = index.compareTo(other.index)
}