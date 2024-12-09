package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.obj.elf.*
import cengine.util.integer.Size
import cengine.util.newint.BigInt
import emulator.EmuLink

object T6502Spec : TargetSpec<ELFGenerator> {
    override val name: String = "6502 MOS"

    override val ei_class: Elf_Byte = E_IDENT.ELFCLASS32
    override val ei_data: Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: Elf_Byte = Ehdr.EV_CURRENT.toUByte()
    override val e_machine: Elf_Half = Ehdr.EM_CUSTOM_T6502

    override val linkerScript: LinkerScript = object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt = 0x4000U
    }
    override val emuLink: EmuLink = EmuLink.T6502

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
    override val allDirs: List<DirTypeInterface> = ASDirType.entries
    override fun createGenerator(): ELFGenerator = ExecELFGenerator(this)

    override fun toString(): String = name
}