package cengine.lang.asm.ast.target.ikrmini

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.elf.*
import cengine.util.integer.Hex
import cengine.util.integer.Size

data object IKRMiniSpec: TargetSpec {
    override val name: String = "IKR Mini"
    override val ei_class: Elf_Byte = E_IDENT.ELFCLASS32
    override val ei_data: Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: Elf_Byte = Ehdr.EV_CURRENT.toUByte()
    override val e_machine: Elf_Half = Ehdr.EM_CUSTOM_IKRMINI

    override val linkerScript: LinkerScript = object : LinkerScript {
        override val textStart: Hex = Hex("0", Size.Bit16)
        override val dataStart: Hex? = null
        override val rodataStart: Hex? = null
        override val segmentAlign: UInt = 0x4000U
    }

    override val memAddrSize: Size = Size.Bit16
    override val wordSize: Size = Size.Bit16
    override val detectRegistersByName: Boolean = false
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""[a-zA-Z._][a-zA-Z0-9._]*""")
    }
    override val allRegs: List<RegTypeInterface> = emptyList()
    override val allInstrs: List<InstrTypeInterface> = IKRMiniInstrType.entries
    override val customDirs: List<DirTypeInterface> = emptyList()
    override fun toString(): String = name
}