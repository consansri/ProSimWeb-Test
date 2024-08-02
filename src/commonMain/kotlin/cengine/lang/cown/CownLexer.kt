package cengine.lang.cown

import cengine.psi.lexer.core.Token
import cengine.psi.lexer.impl.BaseLexer

class CownLexer(input: String): BaseLexer(input) {

    companion object{
        val keywords = listOf(
            "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
            "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return",
            "super", "this", "throw", "true", "try", "typealias", "val", "var", "when", "while",
            "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
            "get", "import", "init", "param", "property", "receiver", "set", "setparam",
            "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
            "data", "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
            "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected",
            "public", "reified", "sealed", "suspend", "tailrec", "vararg"
        )
    }

    override fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): Token {
        TODO("Not yet implemented")
    }

    override fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): Token {
        TODO("Not yet implemented")
    }


}