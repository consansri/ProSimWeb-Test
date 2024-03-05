package emulator.archs.t6502

import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

class T6502Assembly : StandardAssembler(T6502.MEM_ADDR_SIZE, T6502.WORD_SIZE, instrsAreWordAligned = false) {
    override fun getInstrSpace(arch: emulator.kit.Architecture, instr: StandardSyntax.EInstr): Int {
        if (instr !is T6502Syntax.T6502Instr) {
            arch.getConsole().error("Expected [${IKRMiniSyntax.IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        val t6502Instr: T6502Syntax.T6502Instr = instr
        return t6502Instr.addressingMode.byteAmount
    }

    override fun getOpBinFromInstr(arch: emulator.kit.Architecture, instr: StandardSyntax.EInstr): Array<Bin> {
        if (instr !is T6502Syntax.T6502Instr) {
            arch.getConsole().error("Expected [${IKRMiniSyntax.IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        val t6502Instr: T6502Syntax.T6502Instr = instr
        return t6502Instr.getOpBin(arch).map { it.toBin() }.toTypedArray()
    }

    override fun getInstrFromBinary(arch: emulator.kit.Architecture, currentAddress: Hex): ResolvedInstr? {
        val threeByte = arch.getMemory().loadArray(currentAddress, 3)

        var paramType: T6502Syntax.AModes? = null
        val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
            paramType = type.opCode.entries.firstOrNull { threeByte.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase() }?.key
            paramType != null
        }
        val actualParamType = paramType
        if (actualParamType == null || instrType == null) {
            return null
        }
        val ext: String = actualParamType.getString(threeByte)
        return ResolvedInstr(instrType.name, ext, actualParamType.byteAmount)
    }

}