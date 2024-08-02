package cengine.lang.asm.specific.rv32

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmLexer
import emulator.archs.riscv32.RV32
import emulator.core.Size
import emulator.kit.assembler.gas.GASParser
import emulator.kit.common.RegContainer
import emulator.kit.optional.Feature

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
    override val allRegs: List<RegContainer.Register> = RV32.standardRegFile.unsortedRegisters.toList()

    override fun instrTypes(features: List<Feature>): List<InstrTypeInterface> = listOf()

    override fun allInstrTypes(): List<InstrTypeInterface> = listOf()

    override fun additionalDirectives(): List<DirTypeInterface> = listOf()

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        TODO("Not yet implemented")
    }
}