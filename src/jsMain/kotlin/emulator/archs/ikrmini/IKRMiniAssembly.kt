package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable

class IKRMiniAssembly : StandardAssembler(IKRMini.MEM_ADDRESS_WIDTH, IKRMini.WORDSIZE, instrsAreWordAligned = true) {
    override fun getInstrSpace(arch: Architecture, instr: StandardSyntax.EInstr): Int {
        if (instr !is IKRMiniSyntax.IKRMiniInstr) {
            arch.getConsole().error("Expected [${IKRMiniSyntax.IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        val ikrInstr: IKRMiniSyntax.IKRMiniInstr = instr
        return ikrInstr.paramType.wordAmount
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: StandardSyntax.EInstr): Array<Variable.Value.Bin> {
        if (instr !is IKRMiniSyntax.IKRMiniInstr) {
            arch.getConsole().error("Expected [${IKRMiniSyntax.IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        val ikrInstr: IKRMiniSyntax.IKRMiniInstr = instr
        return ikrInstr.getOpBin(arch)
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): ResolvedInstr? {
        val loaded = arch.getMemory().loadArray(currentAddress, IKRMini.WORDSIZE.getByteCount() * 2)
        val opCode = Variable.Value.Hex(loaded.take(2).joinToString("") { it.toHex().getRawHexStr() }, IKRMini.WORDSIZE)
        val ext = Variable.Value.Hex(loaded.drop(2).joinToString("") { it.toHex().getRawHexStr() }, IKRMini.WORDSIZE).getRawHexStr()

        var paramType: IKRMiniSyntax.ParamType? = null
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { type ->
            arch.getConsole().info("${opCode.toString()} -> ${type.name} search in ${type.paramMap.entries.joinToString(",") { it.value.toString() } }")
            paramType = type.paramMap.entries.firstOrNull { opCode.getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase() }?.key
            paramType != null
        }

        val actualParamType = paramType
        if (actualParamType == null || instrType == null) {
            return null
        }


        val extString: String = when (actualParamType) {
            IKRMiniSyntax.ParamType.INDIRECT -> "(($${ext.uppercase()}))"
            IKRMiniSyntax.ParamType.DIRECT -> "($${ext.uppercase()})"
            IKRMiniSyntax.ParamType.IMMEDIATE -> "#$${ext.uppercase()}"
            IKRMiniSyntax.ParamType.DESTINATION -> "$${ext.uppercase()}"
            IKRMiniSyntax.ParamType.IMPLIED -> ""
            null -> ""
        }

        return ResolvedInstr(instrType.name, extString, actualParamType.wordAmount)
    }

}