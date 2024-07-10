package cengine.lexer.core

/**
 * Interface for a lexer that produces tokens.
 */
interface Lexer {
    fun nextToken(): Token
    fun hasMoreTokens(): Boolean
}