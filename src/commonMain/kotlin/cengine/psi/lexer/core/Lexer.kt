package cengine.psi.lexer.core

/**
 * Interface for a lexer that produces tokens.
 */
interface Lexer {

    val ignored: Set<Token>

    fun reset(input: String)
    fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean = true): Token
    fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean = true): Token
    fun peekChar(): Char?
    fun hasMoreTokens(): Boolean

    class InvalidCharException(val invalidChar: Char?, val index: Int) : Exception("$invalidChar at $index is invalid!")
}