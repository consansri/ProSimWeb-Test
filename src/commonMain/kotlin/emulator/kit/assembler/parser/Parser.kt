package emulator.kit.assembler.parser

import debug.DebugTools
import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.CompilerInterface
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.optional.FileHandler

abstract class Parser(val compiler: CompilerInterface) {

    val treeCache: MutableMap<CompilerFile, ParserTree> = mutableMapOf()
    abstract fun getInstrs(features: List<Feature>): List<InstrTypeInterface>
    abstract fun getDirs(features: List<Feature>): List<DirTypeInterface>
    abstract fun parse(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): ParserTree
    data class SearchResult(val baseNode: Node.BaseNode, val path: List<Node>)
    data class ParserError(val token: Token, override val message: String): Exception(message)

    /**
     * Used to hold the identification of editor lines in [FileHandler.File]'s with memory addresses
     */
    data class LineMap(val lineAddressMap: Map<String, Token.LineLoc> = mapOf()) {
        init {
            if (DebugTools.KIT_showAsmInfo) {
                println("Assembly.AssemblyMap(): $lineAddressMap")
            }
        }
    }
}