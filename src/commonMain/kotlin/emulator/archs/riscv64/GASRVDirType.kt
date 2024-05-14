package emulator.archs.riscv64

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.DefinedAssembly
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Token

enum class GASRVDirType(override val isSection: Boolean = false, override val rule: Rule) : DirTypeInterface {

    ;

    override fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive? {

        return null
    }

    override fun getDetectionString(): String = this.name


}