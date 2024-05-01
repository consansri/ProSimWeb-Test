package emulator.kit.assembler.gas.riscv

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASDirType
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

enum class GASRVDirType(override val isSection: Boolean = false, override val rule: Rule) : DirTypeInterface {

    ;

    override fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive? {

        return null
    }

    override fun getDetectionString(): String = this.name


}