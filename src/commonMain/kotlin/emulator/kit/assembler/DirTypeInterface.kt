package emulator.kit.assembler

import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

interface DirTypeInterface {

    /**
     * Leave blank if it isn't starting with the directive itself!
     */
    fun getDetectionString(): String
    val isSection: Boolean
    val rule: Rule?
    fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive?

    /**
     * Execute the usage of a macro!
     *
     * Return a section to change the current section!
     */
    fun executeDirective(
        dirStatement: GASNode.Statement.Dir,
        tempContainer: GASParser.TempContainer
    )

}