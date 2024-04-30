package emulator.kit.assembler.gas.riscv

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASDirType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

enum class GASRVDirType(override val isSection: Boolean = false, override val rule: Rule = GASDirType.EMPTY) : DirTypeInterface {
    HALF,
    WORD,
    DWORD,
    DTPRELWORD,
    DTPRELDWORD,
    ULEB128,
    SLEB128,
    OPTION,
    INSN,
    ATTRIBUTE    ;

    override fun buildDirectiveContent(dirName: Token, tokens: List<Token>, definedAssembly: DefinedAssembly): Node? {

        return null
    }

    override fun getDetectionString(): String = this.name


}