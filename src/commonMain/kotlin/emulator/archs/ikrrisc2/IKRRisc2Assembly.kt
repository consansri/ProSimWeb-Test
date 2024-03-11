package emulator.archs.ikrrisc2

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable

class IKRRisc2Assembly : StandardAssembler(wordWidth = IKRRisc2.WORD_WIDTH, memAddressWidth = IKRRisc2.WORD_WIDTH, instrsAreWordAligned = true) {
    override fun getInstrSpace(arch: Architecture, instr: StandardSyntax.EInstr): Int {
        TODO("Not yet implemented")
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: StandardSyntax.EInstr): Array<Variable.Value.Bin> {
        TODO("Not yet implemented")
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): ResolvedInstr? {
        TODO("Not yet implemented")
    }

}