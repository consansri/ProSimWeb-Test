package cengine.lexer.impl

import cengine.lexer.core.Position
import cengine.lexer.core.Severity
import cengine.lexer.core.Token
import cengine.lexer.core.TokenType

data class BasicToken(
    override val type: TokenType,
    override val value: String,
    override val start: Position,
    override val end: Position
):Token {
    override val severities: MutableList<Severity> = mutableListOf()

    override fun addSeverity(type: Severity.Type, message: String) {
        severities.add(Severity(type, message))
    }

    override fun removeSeverityIfError() {
        severities.removeAll { it.type == Severity.Type.ERROR }
    }
}