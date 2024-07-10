package cengine.lexer.impl

import cengine.lexer.core.Lexer

/**
 * Abstract base class for language-specific lexers.
 */
abstract class BaseLexer(
    protected val input: String
) : Lexer {

    companion object {
        const val LINE_START = 1
        const val COLUMN_START = 1
    }

    protected var position = 0
    protected var line = LINE_START
    protected var column = COLUMN_START

    protected fun advance() {
        if (input[position] == '\n') {
            line++
            column = COLUMN_START
        } else {
            column++
        }
        position++
    }

    protected fun peek(): Char? = if(hasMoreTokens()) input[position] else null
    protected fun peekNext(): Char? = if(position + 1 < input.length) input[position + 1] else null

    override fun hasMoreTokens(): Boolean = position < input.length
}