package cengine.psi.core

/**
 * Represents a position by line and column in a text.
 */
data class TextPosition(val index: Int = 0) : Comparable<TextPosition> {
    operator fun rangeUntil(end: TextPosition): TextRange = TextRange(this, end)
    operator fun plus(other: TextPosition): TextPosition = TextPosition(index + other.index)
    operator fun plus(other: Int): TextPosition = TextPosition(index + other)
    operator fun minus(other: TextPosition): TextPosition = TextPosition(index - other.index)
    override fun compareTo(other: TextPosition): Int = index.compareTo(other.index)
    override fun toString(): String = index.toString()

    override fun equals(other: Any?): Boolean {
        if (other !is TextPosition) return false
        if (other.index != index) return false
        return true
    }

    override fun hashCode(): Int {
        return index
    }
}