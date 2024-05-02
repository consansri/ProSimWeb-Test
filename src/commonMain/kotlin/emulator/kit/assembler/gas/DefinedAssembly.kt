package emulator.kit.assembler.gas

import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

interface DefinedAssembly {

    val MEM_ADDRESS_SIZE: Variable.Size
    val WORD_SIZE: Variable.Size
    val INSTRS_ARE_WORD_ALIGNED: Boolean
    val detectRegistersByName: Boolean
    val numberPrefixes: NumberPrefixes
    fun getInstrs(features: List<Feature>): List<InstrTypeInterface>
    fun getAdditionalDirectives(): List<DirTypeInterface>
    fun getInstrSpace(arch: emulator.kit.Architecture, instr: GASNode.Instruction): Int
    fun getOpBinFromInstr(arch: emulator.kit.Architecture, instr: GASNode.Instruction): Array<Variable.Value.Bin>
    fun getInstrFromBinary(arch: emulator.kit.Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr?
    fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instruction?

    interface NumberPrefixes{
        val hex: String
        val dec: String
        val bin: String
    }

}