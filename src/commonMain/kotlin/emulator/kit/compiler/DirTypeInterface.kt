package emulator.kit.compiler

import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node

interface DirTypeInterface {
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule
    fun buildDirectiveContent(dirName: Token.KEYWORD.Directive, tokens: List<Token>, definedAssembly: DefinedAssembly): Node?
}