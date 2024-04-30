package emulator.archs.ikrrisc2

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class IKRRisc2Assembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size
        get() = TODO("Not yet implemented")
    override val WORD_SIZE: Variable.Size
        get() = TODO("Not yet implemented")
    override val INSTRS_ARE_WORD_ALIGNED: Boolean
        get() = TODO("Not yet implemented")
    override val detectRegistersByName: Boolean
        get() = TODO("Not yet implemented")
    override val numberPrefixes: DefinedAssembly.NumberPrefixes
        get() = TODO("Not yet implemented")

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
        TODO("Not yet implemented")
    }

    override fun getAdditionalDirectives(): List<DirTypeInterface> {
        TODO("Not yet implemented")
    }

    override fun getInstrSpace(arch: Architecture, instr: GASNode.Instr): Int {
        TODO("Not yet implemented")
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: GASNode.Instr): Array<Variable.Value.Bin> {
        TODO("Not yet implemented")
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr? {
        TODO("Not yet implemented")
    }

    override fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instr? {
        TODO("Not yet implemented")
    }
}