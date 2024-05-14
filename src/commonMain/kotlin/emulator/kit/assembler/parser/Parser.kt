package emulator.kit.assembler.parser

import emulator.kit.assembler.*
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature

abstract class Parser(val compiler: Compiler) {

    val treeCache: MutableMap<CompilerFile, TreeResult> = mutableMapOf()
    abstract fun getInstrs(features: List<Feature>): List<InstrTypeInterface>
    abstract fun getDirs(features: List<Feature>): List<DirTypeInterface>
    abstract fun parseTree(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): TreeResult
    abstract fun semanticAnalysis(lexer: Lexer, tree: TreeResult, others: List<CompilerFile>, features: List<Feature>): SemanticResult
    data class SearchResult(val baseNode: Node.BaseNode, val path: List<Node>)
    data class ParserError(val token: Token, override val message: String): Exception(message){
        init {
            token.addSeverity(Severity.Type.ERROR, message)
        }
    }

    data class SemanticResult(val sections: Array<GASParser.Section> = arrayOf()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SemanticResult) return false

            if (!sections.contentEquals(other.sections)) return false

            return true
        }

        override fun hashCode(): Int {
            return sections.contentHashCode()
        }

    }
}