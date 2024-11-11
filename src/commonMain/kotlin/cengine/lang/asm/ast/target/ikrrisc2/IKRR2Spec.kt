package cengine.lang.asm.ast.target.ikrrisc2

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.obj.elf.*
import cengine.util.integer.Hex
import cengine.util.integer.Size
import emulator.EmuLink

data object IKRR2Spec: TargetSpec {
    override val name: String = "IKR RISC-II"
    override val ei_class: Elf_Byte = E_IDENT.ELFCLASS32
    override val ei_data: Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: Elf_Byte = Ehdr.EV_CURRENT.toUByte()
    override val e_machine: Elf_Half = Ehdr.EM_CUSTOM_IKRRISC2
    override val emuLink: EmuLink = EmuLink.IKRRISC2

    override val linkerScript: LinkerScript = object : LinkerScript {
        override val textStart: Hex = Hex("0", Size.Bit32)
        override val dataStart: Hex? = null
        override val rodataStart: Hex? = null
        override val segmentAlign: UInt = 0x10000U
    }

    override val memAddrSize: Size = Size.Bit32
    override val wordSize: Size = Size.Bit32
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
    override fun toString(): String = name
}