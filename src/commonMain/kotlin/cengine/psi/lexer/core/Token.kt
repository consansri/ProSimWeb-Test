package cengine.psi.lexer.core

import cengine.psi.core.Locatable
import cengine.psi.core.TextPosition
import cengine.psi.core.TextRange

/**
 * Interface representing a token in the source code.
 */
abstract class Token : Locatable {
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
    abstract val start: TextPosition

    /**
     * The ending position of the token in the source code.
     */
    abstract val end: TextPosition

    /**
     * Builds a TextRange object from [start] and [end] index.
     */
    override val textRange: TextRange
        get() = TextRange(start, end)

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
