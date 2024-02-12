package emulator.archs.riscv64

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable

class RV64Assembly(val binMapper: RV64BinMapper) : StandardAssembler(RV64.MEM_ADDRESS_WIDTH, RV64.WORD_WIDTH, instrsAreWordAligned = true) {
    override fun getInstrSpace(arch: Architecture, instr: StandardSyntax.EInstr): Int {
        if (instr !is RV64Syntax.RV64Instr) {
            arch.getConsole().error("Expected [${RV64Syntax.RV64Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        return instr.instrType.memWords
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: StandardSyntax.EInstr): Array<Variable.Value.Bin> {
        if (instr !is RV64Syntax.RV64Instr) {
            arch.getConsole().error("Expected [${RV64Syntax.RV64Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        return binMapper.getBinaryFromInstrDef(instr, arch)

    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): ResolvedInstr? {
        val opCode = Variable.Value.Bin(arch.getMemory().loadArray(currentAddress, RV64.WORD_WIDTH.getByteCount()).joinToString("") { it.toBin().getRawBinStr() }, RV64.WORD_WIDTH)
        val result = binMapper.getInstrFromBinary(opCode) ?: return null
        return ResolvedInstr(result.type.id, result.type.paramType.getTSParamString(arch, result.binMap.toMutableMap()),1)
    }

}