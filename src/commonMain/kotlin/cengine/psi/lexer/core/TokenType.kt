package cengine.psi.lexer.core

import cengine.lang.asm.CodeStyle

/**
 * Interface representing a token type.
 * This allows for easy extension for different languages.
 */
interface TokenType {
    val name: String
    val style: CodeStyle?
}