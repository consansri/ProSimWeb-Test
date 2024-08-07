package cengine.lang.asm.target.rv32

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.lexer.AsmLexer
import emulator.core.Size

class RV32Spec: AsmSpec {
    override val memAddrSize: Size = Size.Bit32
    override val wordSize: Size = Size.Bit32
    override val addrShift: Int = 0
    override val detectRegistersByName: Boolean = true
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices{
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }
    override val allRegs: List<RegTypeInterface> = RVBaseRegs.entries
    override val allInstrs: List<InstrTypeInterface> = RV32InstrTypes.entries
    override val customDirs: List<DirTypeInterface> = RVDirType.entries

}