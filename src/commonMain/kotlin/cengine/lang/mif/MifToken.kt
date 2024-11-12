package cengine.lang.mif

import cengine.psi.lexer.core.Token
import cengine.psi.lexer.core.TokenType

data class MifToken(
    override val type: Type,
    override val value: String,
    override var range: IntRange
): Token() {
    enum class Type: TokenType{
        KEYWORD,
        RADIX,
        IDENTIFIER,
        NUMBER,
        SYMBOL,
        COMMENT,
        UNKNOWN,
        EOF
    }
}