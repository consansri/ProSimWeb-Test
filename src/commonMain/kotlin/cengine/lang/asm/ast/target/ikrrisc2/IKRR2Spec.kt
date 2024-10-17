package cengine.lang.asm.ast.target.ikrrisc2

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.elf.Elf_Byte
import cengine.lang.asm.elf.Elf_Half
import cengine.lang.asm.elf.Elf_Xword
import cengine.lang.asm.elf.LinkerScript
import cengine.util.integer.Hex
import cengine.util.integer.Size

data object IKRR2Spec: TargetSpec {
    override val name: String = "IKR RISC-II"
    override val ei_class: Elf_Byte
        get() = TODO("Not yet implemented")
    override val ei_data: Elf_Byte
        get() = TODO("Not yet implemented")
    override val ei_osabi: Elf_Byte
        get() = TODO("Not yet implemented")
    override val ei_abiversion: Elf_Byte
        get() = TODO("Not yet implemented")
    override val e_machine: Elf_Half
        get() = TODO("Not yet implemented")
    override val e_text_addr: Elf_Xword
        get() = TODO("Not yet implemented")
    override val e_data_addr: Elf_Xword
        get() = TODO("Not yet implemented")

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