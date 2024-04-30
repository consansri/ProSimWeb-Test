package emulator.kit.compiler.parser

import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token

class ParserTree(val rootNode: Node?, val source: List<Token>, val treeRelevantTokens: List<Token>) {
    fun contains(token: Token): Parser.SearchResult? = rootNode?.searchBaseNode(token, listOf())

    fun hasErrors(): Boolean {
        if (rootNode == null) return false
        source.forEach {
            if (it.getMajorSeverity()?.type == Severity.Type.ERROR) return true
        }
        return false
    }

    fun printError(): String? {
        return source.mapNotNull { it.printError() }.ifEmpty { return null }.joinToString("\n") { it }
    }

    override fun toString(): String {
        return rootNode?.print("") ?: "[null]"
    }
}