package cengine.lexer.core

/**
 * Interface representing a token in the source code.
 */
interface Token {

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
    val start: Position

    /**
     * The ending position of the token in the source code.
     */
    val end: Position

    /**
     * List of severities associated with this token.
     */
    val severities: List<Severity>

    /**
     * Adds a severity to the token.
     */
    fun addSeverity(type: Severity.Type, message: String)

    /**
     * Removes all error severities from the token.
     */
    fun removeSeverityIfError()
}