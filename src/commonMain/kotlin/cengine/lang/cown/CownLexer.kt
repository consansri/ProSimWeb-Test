package cengine.lang.cown

import cengine.lexer.core.Token
import cengine.lexer.impl.BaseLexer

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


    override fun nextToken(): Token {
        TODO()
    }


}