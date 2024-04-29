package emulator.kit.compiler.gas.riscv

import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.Rule
import emulator.kit.compiler.gas.GASDirType
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node

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

    override fun buildDirectiveContent(dirName: Token.KEYWORD.Directive, tokens: List<Token>): Node? {

        return null
    }

    override fun getDetectionString(): String = this.name


}