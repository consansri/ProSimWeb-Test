package emulator.kit.compiler.parser

import emulator.kit.compiler.CompilerFile
import emulator.kit.compiler.CompilerInterface
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.lexer.Token
import emulator.kit.optional.Feature

abstract class Parser(val compiler: CompilerInterface) {

    val treeCache: MutableMap<CompilerFile, ParserTree> = mutableMapOf()
    abstract fun getInstrs(features: List<Feature>): List<InstrTypeInterface>
    abstract fun getDirs(features: List<Feature>): List<DirTypeInterface>
    abstract fun parse(tokens: List<Token>, others: List<CompilerFile>): ParserTree

    data class SearchResult(val baseNode: Node.BaseNode, val path: List<Node>)
}