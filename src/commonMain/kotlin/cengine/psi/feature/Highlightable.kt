package cengine.psi.feature

import cengine.lang.asm.CodeStyle
import cengine.psi.lexer.core.TokenType

interface Highlightable {
    val style: CodeStyle?

    enum class Type(val style: CodeStyle) {
        KEYWORD(CodeStyle.ORANGE),
        FUNCTION(CodeStyle.BLUE),
        IDENTIFIER(CodeStyle.MAGENTA),
        STRING(CodeStyle.ALTGREEN),
        COMMENT(CodeStyle.comment),
        NUMBER_LITERAL(CodeStyle.ALTBLUE)
    }
}