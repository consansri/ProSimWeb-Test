package cengine.lang.asm.ast.target.riscv.rv32

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.target.riscv.RVBaseRegs
import cengine.lang.asm.ast.target.riscv.RVCsr
import cengine.lang.asm.ast.target.riscv.RVDirType
import cengine.lang.obj.elf.*
import cengine.util.integer.Hex
import cengine.util.integer.Size

data object RV32Spec : TargetSpec {
    override val name: String = "RISC-V 32 Bit"

    override val ei_class: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFCLASS32
    override val ei_data: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ZERO
    override val e_machine: cengine.lang.obj.elf.Elf_Half = Ehdr.EM_RISCV
    override val linkerScript: LinkerScript = object : LinkerScript{
        override val textStart: Hex = Hex("0", Size.Bit32)
        override val dataStart: Hex? = null
        override val rodataStart: Hex? = null
        override val segmentAlign: UInt = 0x40000U
    }

    override val memAddrSize: Size = Size.Bit32
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
    override val allInstrs: List<InstrTypeInterface> = RV32InstrType.entries
    override val customDirs: List<DirTypeInterface> = RVDirType.entries
    override fun toString(): String = name
}