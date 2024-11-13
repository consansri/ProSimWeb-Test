package cengine.lang.mif.ast

import cengine.lang.asm.CodeStyle
import cengine.psi.feature.Highlightable
import cengine.psi.lexer.core.Token
import cengine.psi.lexer.core.TokenType

data class MifToken(
    override val type: Type,
    override val value: String,
    override var range: IntRange
) : Token() {
    enum class Type(override val style: CodeStyle? = null) : TokenType {
        KEYWORD(Highlightable.Type.KEYWORD.style),
        RADIX(Highlightable.Type.STRING.style),
        IDENTIFIER(Highlightable.Type.IDENTIFIER.style),
        NUMBER(Highlightable.Type.NUMBER_LITERAL.style),
        RANGE_TO,
        SYMBOL,
        COMMENT(Highlightable.Type.COMMENT.style),
        UNKNOWN,
        EOF
    }
}