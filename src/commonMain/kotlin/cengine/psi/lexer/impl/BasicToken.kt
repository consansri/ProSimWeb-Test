package cengine.psi.lexer.impl

import cengine.psi.core.TextPosition
import cengine.psi.lexer.core.Token
import cengine.psi.lexer.core.TokenType

data class BasicToken(
    override val type: TokenType,
    override val value: String,
    override val start: TextPosition,
    override val end: TextPosition
) : Token(){

}