package cengine.psi.lexer.impl

import cengine.psi.core.TextPosition
import cengine.psi.lexer.core.Lexer
import cengine.psi.lexer.core.Token

/**
 * Abstract base class for language-specific lexers.
 */
abstract class BaseLexer(
    protected var input: String
) : Lexer {

    protected var index = 0
    protected var line = 0
    protected var col = 0
    var position: TextPosition
        set(value) {

            index = value.index
        }
        get() = TextPosition(index)

    override val error: MutableList<Token> = mutableListOf()
    override val ignored: MutableList<Token> = mutableListOf()

    override fun reset(input: String) {
        error.clear()
        ignored.clear()
        this.input = input
        index = 0
    }

    protected fun advance() {
        index = (index + 1).coerceAtMost(input.length)
    }

    protected fun advance(amount: Int) {
        index = (index + amount).coerceAtMost(input.length)
    }

    protected fun skipSpaces() {
        while (hasMoreTokens() && peekChar() == ' ') {
            advance()
        }
    }

    override fun peekChar(): Char? = if (hasMoreTokens()) input[index] else null

    override fun hasMoreTokens(): Boolean = index < input.length
}