package emulator.kit.assembler.parser

import emulator.kit.assembler.*
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature

/**
 * Abstract class representing a parser for assembly language.
 *
 * @property compiler The compiler instance associated with the parser.
 */
abstract class Parser(val compiler: Compiler) {

    /** Cache for storing parsed tree results. */
    val treeCache: MutableMap<CompilerFile, TreeResult> = mutableMapOf()

    /**
     * Retrieves the instruction types based on the provided features.
     *
     * @param features The optional features influencing instruction retrieval.
     * @return A list of instruction type interfaces.
     */
    abstract fun getInstrs(features: List<Feature>): List<InstrTypeInterface>

    /**
     * Retrieves the directive types based on the provided features.
     *
     * @param features The optional features influencing directive retrieval.
     * @return A list of directive type interfaces.
     */
    abstract fun getDirs(features: List<Feature>): List<DirTypeInterface>

    /**
     * Parses the source tokens into a parse tree.
     *
     * @param source The list of source tokens to parse.
     * @param others Additional compiler files.
     * @param features The optional features influencing parsing.
     * @return The parse tree result.
     */
    abstract fun parseTree(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): TreeResult

    /**
     * Performs semantic analysis on the parse tree.
     *
     * @param lexer The lexer instance.
     * @param tree The parse tree result.
     * @param others Additional compiler files.
     * @param features The optional features influencing semantic analysis.
     * @return The semantic analysis result.
     */
    abstract fun semanticAnalysis(lexer: Lexer, tree: TreeResult, others: List<CompilerFile>, features: List<Feature>): SemanticResult

    /** Data class representing the search result during parsing. */
    data class SearchResult(val baseNode: Node.BaseNode, val path: List<Node>)

    /**
     * Data class representing a parser error.
     *
     * @property token The token associated with the error.
     * @property message The error message.
     */
    data class ParserError(val token: Token, override val message: String): Exception(message){
        init {
            token.addSeverity(Severity.Type.ERROR, message)
        }
    }

    /**
     * Data class representing the result of semantic analysis.
     *
     * @property sections Array of GAS sections.
     */
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