package emulator.kit.assembler.parser

import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token

class ParserTree(val rootNode: Node?, val source: List<Token>, val treeRelevantTokens: List<Token>, val sections: Array<GASParser.Section>) {
    fun contains(token: Token): Parser.SearchResult? = rootNode?.searchBaseNode(token, listOf())

    fun hasErrors(): Boolean {
        if (rootNode == null) return false
        source.forEach {
            if (it.getMajorSeverity()?.type == Severity.Type.ERROR) return true
        }
        return false
    }

    fun hasWarnings(): Boolean{
        if(rootNode == null) return false
        source.forEach {
            if(it.getMajorSeverity()?.type == Severity.Type.WARNING) return true
        }
        return false
    }

    fun printError(): String? {
        return source.mapNotNull { it.printError() }.ifEmpty { return null }.joinToString("\n") { it }
    }

    fun printWarning(): String? {
        return source.mapNotNull { it.printWarning() }.ifEmpty { return null }.joinToString("\n") { it }
    }

    override fun toString(): String {
        return rootNode?.print("") ?: "[null]"
    }
}