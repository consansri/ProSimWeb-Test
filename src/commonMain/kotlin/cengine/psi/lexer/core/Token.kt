package cengine.psi.lexer.core

import cengine.psi.core.Interval

/**
 * Interface representing a token in the source code.
 */
abstract class Token : Interval {
    /**
     * The type of the token.
     */
    abstract val type: TokenType

    /**
     * The content of the token.
     */
    abstract val value: String

    /**
     * The starting position of the token in the source code.
     */
    abstract val start: Int

    /**
     * The ending position of the token in the source code.
     */
    abstract val end: Int

    /**
     * Builds a TextRange object from [start] and [end] index.
     */
    override val range: IntRange
        get() = start..<end

    override fun equals(other: Any?): Boolean {
        if (other !is Token) return false

        if(other.type != type) return false
        if(other.start != start) return false
        if(other.end != end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
}
