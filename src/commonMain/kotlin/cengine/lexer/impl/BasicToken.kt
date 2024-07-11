package cengine.lexer.impl

import cengine.lexer.core.Position
import cengine.lexer.core.Token
import cengine.lexer.core.TokenType

data class BasicToken(
    override val type: TokenType,
    override val value: String,
    override val start: Position,
    override val end: Position
):Token {

}