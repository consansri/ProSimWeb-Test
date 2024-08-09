package cengine.lang.asm.ast.target.risc2

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.lexer.AsmLexer
import emulator.core.Size

object IKRR2Spec: AsmSpec {
    override val memAddrSize: Size = Size.Bit32
    override val wordSize: Size = Size.Bit32
    override val addrShift: Int = 2
    override val detectRegistersByName: Boolean = true
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = ";"
        override val symbol: Regex = Regex("""[a-zA-Z._][a-zA-Z0-9$._]*""")
    }
    override val allRegs: List<RegTypeInterface> = IKRR2BaseRegs.entries
    override val allInstrs: List<InstrTypeInterface> = IKRR2InstrType.entries
    override val customDirs: List<DirTypeInterface> = emptyList()
}