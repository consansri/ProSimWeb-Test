package cengine.lang.asm.lexer

import cengine.lang.asm.lexer.AsmTokenType.*
import cengine.psi.core.TextPosition
import cengine.psi.lexer.core.Token


data class AsmToken(
    override val type: AsmTokenType,
    override val value: String,
    override val start: TextPosition,
    override val end: TextPosition,
    val onlyNumber: String? = null
) : Token {

    val asNumber: String get() = onlyNumber ?: value

    /**
     * Gets the value of the token as a string.
     */
    fun getContentAsString(): String = when (type) {
        WHITESPACE -> ""
        LINEBREAK -> "\n"
        COMMENT_SL -> ""
        COMMENT_ML -> ""
        STRING_ML -> value.substring(3, value.length - 3).replaceEscapedChars()
        STRING_SL -> value.substring(1, value.length - 1).replaceEscapedChars()
        CHAR -> value.substring(1).replaceEscapedChars()
        ARG_REF -> value.substring(1)
        ARG_SEPARATOR -> ""
        COMMENT_NATIVE -> ""
        DIRECTIVE -> value.removePrefix(".").lowercase()
        else -> value
    }

    private fun String.replaceEscapedChars(): String {
        var result = this
        EscapedChar.entries.forEach {
            result = result.replace(it.id, it.replacement)
        }
        return result
    }

    /**
     * Checks if the token has a higher or equal precedence compared to another token.
     */
    fun higherOrEqualPrecedenceAs(other: AsmToken, markedAsPrefix: List<AsmToken>): Boolean {
        val thisPrecedence = getPrecedence(markedAsPrefix) ?: return false
        val otherPrecedence = other.getPrecedence(markedAsPrefix) ?: return false
        return thisPrecedence.ordinal >= otherPrecedence.ordinal
    }

    /**
     * Gets the precedence of the token.
     */
    fun getPrecedence(markedAsPrefix: List<AsmToken>): Enum<*>? {
        return when (type) {
            COMPLEMENT -> Precedence.PREFIX
            MULT -> Precedence.HIGH
            DIV -> Precedence.HIGH
            REM -> Precedence.HIGH
            SHL -> Precedence.HIGH
            SHR -> Precedence.HIGH
            BITWISE_OR -> Precedence.INTERMEDIATE
            BITWISE_AND -> Precedence.INTERMEDIATE
            BITWISE_XOR -> Precedence.INTERMEDIATE
            BITWISE_ORNOT -> Precedence.INTERMEDIATE
            PLUS -> if (markedAsPrefix.contains(this)) Precedence.PREFIX else Precedence.LOW
            MINUS -> if (markedAsPrefix.contains(this)) Precedence.PREFIX else Precedence.LOW
            else -> null
        }
    }

    /**
     * Represents pairs of opening and closing brackets.
     */
    enum class BracketPairs(val opening: AsmTokenType, val closing: AsmTokenType) {
        BASIC(BRACKET_OPENING, BRACKET_CLOSING),
        CURLY(CURLY_BRACKET_OPENING, CURLY_BRACKET_CLOSING),
        SQUARE(SQUARE_BRACKET_OPENING, SQUARE_BRACKET_CLOSING)
    }

    /**
     * Represents the precedence of a token.
     */
    enum class Precedence {
        LOWEST,
        LOW,
        INTERMEDIATE,
        HIGH,
        PREFIX,
    }

    /**
     * Represents escaped characters.
     */
    enum class EscapedChar(val id: String, val replacement: String) {
        N("\\n", "\n"),
        T("\\t", "\t"),
        B("\\b", "\b"),
        R("\\r", "\r"),
        F("\\f", "\u000c"),
        II("\\\"", "\""),
        I("\\\'", "\'"),
        SLASH("\\\\", "\\");

        val regex: Regex = Regex("^${Regex.escape(id)}")
    }

    override fun toString(): String = "{${type.name}:$value}"
}