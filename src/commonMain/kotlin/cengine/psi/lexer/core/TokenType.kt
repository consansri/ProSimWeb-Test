package cengine.psi.lexer.core

/**
 * Interface representing a token type.
 * This allows for easy extension for different languages.
 */
interface TokenType {
    val name: String
}