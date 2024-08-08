package cengine.psi.lexer.impl

import cengine.psi.lexer.core.Lexer
import cengine.psi.lexer.core.Token

/**
 * Abstract base class for language-specific lexers.
 */
abstract class BaseLexer(
    protected var input: String
) : Lexer {

    protected var index = 0
    var position: Int
        set(value) {
            index = value
        }
        get() = index

    override val ignored: MutableSet<Token> = mutableSetOf()

    override fun reset(input: String) {
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