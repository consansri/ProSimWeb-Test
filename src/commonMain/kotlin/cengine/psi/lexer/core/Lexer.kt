package cengine.psi.lexer.core

/**
 * Interface for a lexer that produces tokens.
 */
interface Lexer {
    fun reset(input: String)
    fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean = true): Token
    fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean = true): Token
    fun peekChar(): Char?
    fun hasMoreTokens(): Boolean

    class InvalidTokenException(val token: Token, val index: Int) : Exception("$token at $index is invalid!")
}