package emulator.kit.compiler.parser

import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token

class ParserTree(val rootNode: Node?, val source: List<Token>) {

    fun contains(token: Token): Parser.SearchResult? = rootNode?.searchTokenNode(token, listOf())

    fun hasNoErrors(): Boolean {
        source.forEach {
            if (it.getSeverity()?.type == Severity.Type.ERROR) return false
        }
        return true
    }

    override fun toString(): String {
        return rootNode?.print("") ?: "[null]"
    }

}