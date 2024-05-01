package emulator.kit.assembler

import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

interface DirTypeInterface {
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule
    fun buildDirectiveContent(dirName: Token, tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive?
}