package cengine.psi.lexer.core

import cengine.psi.core.Locatable
import cengine.psi.core.TextPosition
import cengine.psi.core.TextRange

/**
 * Interface representing a token in the source code.
 */
interface Token: Locatable {

    /**
     * The type of the token.
     */
    val type: TokenType

    /**
     * The content of the token.
     */
    val value: String

    /**
     * The starting position of the token in the source code.
     */
    val start: TextPosition

    /**
     * The ending position of the token in the source code.
     */
    val end: TextPosition

    /**
     * Builds a TextRange object from [start] and [end] index.
     */
    override val textRange: TextRange
        get() = TextRange(start, end)

}
