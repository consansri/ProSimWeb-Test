package emulator.kit.compiler.gas

import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Token
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
    fun getInstrSpace(arch: emulator.kit.Architecture, instr: GASNode.Instr): Int
    fun getOpBinFromInstr(arch: emulator.kit.Architecture, instr: GASNode.Instr): Array<Variable.Value.Bin>
    fun getInstrFromBinary(arch: emulator.kit.Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr?
    fun parseInstrParams(instrToken: Token.KEYWORD.InstrName, remainingSource: List<Token>): GASNode.Instr?

    interface NumberPrefixes{
        val hex: String
        val dec: String
        val bin: String
    }

}