package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.lexer.AsmLexer
import emulator.core.Size

object T6502Spec : AsmSpec {
    override val name: String = "6502 MOS"
    override val memAddrSize: Size = Size.Bit16
    override val wordSize: Size = Size.Bit16
    override val detectRegistersByName: Boolean = false
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""[a-zA-Z._][a-zA-Z0-9$._]*""")
    }
    override val allRegs: List<RegTypeInterface> = emptyList()
    override val allInstrs: List<InstrTypeInterface> = T6502InstrType.entries
    override val customDirs: List<DirTypeInterface> = emptyList()
    override fun toString(): String = name
}