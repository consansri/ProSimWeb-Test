package cengine.lang.asm.ast.target.riscv.rv64

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.target.riscv.RVBaseRegs
import cengine.lang.asm.ast.target.riscv.RVCsr
import cengine.lang.asm.ast.target.riscv.RVDirType
import cengine.lang.asm.elf.*
import cengine.util.integer.Size

data object RV64Spec : TargetSpec {
    override val name: String = "RISC-V 64 Bit"

    override val ei_class: Elf_Byte = E_IDENT.ELFCLASS64
    override val ei_data: Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: Elf_Byte = E_IDENT.ZERO
    override val e_machine: Elf_Half = Ehdr.EM_RISCV
    override val e_text_addr: Elf_Xword = 0U
    override val e_data_addr: Elf_Xword = 0x4000000000000000U

    override val memAddrSize: Size = Size.Bit64
    override val wordSize: Size = Size.Bit32
    override val detectRegistersByName: Boolean = true
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override val allRegs: List<RegTypeInterface> = RVBaseRegs.entries + RVCsr.regs
    override val allInstrs: List<InstrTypeInterface> = RV64InstrType.entries
    override val customDirs: List<DirTypeInterface> = RVDirType.entries
    override fun toString(): String = name
}