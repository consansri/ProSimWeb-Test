package cengine.lang.asm.ast.target.ikrmini

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.mif.MifGenerator
import cengine.lang.obj.elf.E_IDENT
import cengine.lang.obj.elf.Ehdr
import cengine.lang.obj.elf.LinkerScript
import cengine.util.Endianness
import cengine.util.buffer.Int16Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.Int16
import cengine.util.integer.IntNumberStatic
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object IKRMiniSpec : TargetSpec<MifGenerator<Int16Buffer>> {
    override val name: String = "IKR Mini"
    override val ei_class: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFCLASS32
    override val ei_data: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFDATA2LSB
    override val ei_osabi: cengine.lang.obj.elf.Elf_Byte = E_IDENT.ELFOSABI_SYSV
    override val ei_abiversion: cengine.lang.obj.elf.Elf_Byte = Ehdr.EV_CURRENT.toUInt8()
    override val e_machine: cengine.lang.obj.elf.Elf_Half = Ehdr.EM_CUSTOM_IKRMINI
    override val emuLink: EmuLink = EmuLink.IKRMINI

    override val linkerScript: LinkerScript = object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt64 = 0x4000U.toUInt64()
    }

    override val memAddrSize: IntNumberStatic<*> = Int16
    override val wordSize: IntNumberStatic<*> = Int16
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
    override val allDirs: List<DirTypeInterface> = ASDirType.entries

    override fun createGenerator(): MifGenerator<Int16Buffer> = MifGenerator(linkerScript, memAddrSize) {
        Int16Buffer(Endianness.LITTLE)
    }

    override fun toString(): String = name
}