package cengine.lang.mif.ast

import cengine.psi.lexer.core.Token
import cengine.psi.lexer.impl.BaseLexer

class MifLexer(input: String) : BaseLexer(input) {

    /**
     * For Caching the peeked token at a certain Index
     */
    private var peeked: Pair<Int, Token>? = null

    override fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): MifToken {
        peeked?.let {
            if (it.first == position) return it.second as MifToken
        }
        val initialPos = position
        val token = consume(ignoreLeadingSpaces, ignoreComments)
        peeked = initialPos to token
        position = initialPos
        return token
    }

    override fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): MifToken {
        if (!hasMoreTokens()) {
            return MifToken(MifToken.Type.EOF, "", input.length..input.length)
        }

        if (ignoreLeadingSpaces && (input[position].isWhitespace() || input[position] == '\n')) readWhitespace()

        if (!hasMoreTokens()) {
            return MifToken(MifToken.Type.EOF, "", input.length..input.length)
        }

        if (input.startsWith("--", position)) {
            if (ignoreComments) {
                ignored += readSLComment()
            } else {
                return readSLComment()
            }
        }

        if (ignoreLeadingSpaces && (input[position].isWhitespace() || input[position] == '\n')) readWhitespace()

        if (!hasMoreTokens()) {
            return MifToken(MifToken.Type.EOF, "", input.length..input.length)
        }

        if (input.startsWith("%", position)) {
            if (ignoreComments) {
                ignored += readMLComment()
            } else {
                return readMLComment()
            }
        }

        if (ignoreLeadingSpaces && (input[position].isWhitespace() || input[position] == '\n')) readWhitespace()

        if (!hasMoreTokens()) {
            return MifToken(MifToken.Type.EOF, "", input.length..input.length)
        }

        val currChar = input[position]

        return when {
            input.startsWith("CONTENT", position)
                    || input.startsWith("BEGIN", position)
                    || input.startsWith("END", position)

            -> readKeyword()

            input.startsWith("DEPTH", position)
                    || input.startsWith("WIDTH", position)
                    || input.startsWith("ADDRESS_RADIX", position)
                    || input.startsWith("DATA_RADIX", position)
            -> readIdentifier()

            input.startsWith("BIN", position)
                    || input.startsWith("DEC", position)
                    || input.startsWith("HEX", position)
                    || input.startsWith("OCT", position)
                    || input.startsWith("UNS", position)
            -> readRadix()

            currChar.isDigit() || currChar in "ABCDEFabcdef" -> readNumber()

            input.startsWith("..", position) -> readRangeTo()

            else -> readSymbol()
        }
    }

    private fun readRangeTo(): MifToken {
        val start = position
        val content = input.substring(start, start + 2)
        position += content.length
        return MifToken(MifToken.Type.RANGE_TO, content, start..<position)
    }

    private fun readRadix(): MifToken {
        val start = position
        val content = input.substring(start, start + 3)
        position += content.length
        return MifToken(MifToken.Type.RADIX, content, start..<position)
    }

    private fun readMLComment(): MifToken {
        val start = position
        position = input.indexOf('%', position + 1).takeIf { it != -1 } ?: input.length
        return MifToken(MifToken.Type.COMMENT, input.substring(start, position), start until position)
    }

    private fun readSLComment(): MifToken {
        val start = position
        position = input.indexOf('\n', position).takeIf { it != -1 } ?: input.length
        return MifToken(MifToken.Type.COMMENT, input.substring(start, position), start until position)
    }

    private fun readKeyword(): MifToken {
        val start = position
        while (position < input.length && input[position].isLetter()) position++
        return MifToken(MifToken.Type.KEYWORD, input.substring(start, position), start until position)
    }

    private fun readIdentifier(): MifToken {
        val start = position
        while (position < input.length && (input[position].isLetterOrDigit() || input[position] in "_")) position++
        return MifToken(MifToken.Type.IDENTIFIER, input.substring(start, position), start until position)
    }

    private fun readNumber(): MifToken {
        val start = position
        while (position < input.length && input[position].isDigit() || input[position] in "ABCDEFabcdef") position++
        return MifToken(MifToken.Type.NUMBER, input.substring(start, position), start until position)
    }

    private fun readSymbol(): MifToken {
        val symbol = input[position].toString()
        val token = MifToken(MifToken.Type.SYMBOL, symbol, position until position + 1)
        position++
        return token
    }

    private fun readWhitespace() {
        while ((position < input.length) && (input[position].isWhitespace() || input[position] == '\n')) position++
    }

    private fun readUnknown(): MifToken {
        val unknown = input[position].toString()
        val token = MifToken(MifToken.Type.UNKNOWN, unknown, position until position + 1)
        position++
        return token
    }


}