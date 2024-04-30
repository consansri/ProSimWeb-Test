package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class IKRMiniAssembler: DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size = IKRMini.MEM_ADDRESS_WIDTH
    override val WORD_SIZE: Variable.Size = IKRMini.WORDSIZE
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = false
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes{
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = IKRMiniSyntax.InstrType.entries

    override fun getAdditionalDirectives(): List<DirTypeInterface> = listOf()

    override fun getInstrSpace(arch: Architecture, instr: GASNode.Instr): Int {
        if (instr !is IKRMiniInstr) {
            arch.getConsole().error("Expected [${IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        return instr.paramType.wordAmount
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: GASNode.Instr): Array<Variable.Value.Bin> {
        if (instr !is IKRMiniInstr) {
            arch.getConsole().error("Expected [${IKRMiniInstr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        val ikrInstr: IKRMiniInstr = instr
        return ikrInstr.getOpBin(arch)
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr? {
        val loaded = arch.getMemory().loadArray(currentAddress, IKRMini.WORDSIZE.getByteCount() * 2)
        val opCode = Variable.Value.Hex(loaded.take(2).joinToString("") { it.toHex().getRawHexStr() }, IKRMini.WORDSIZE)
        val ext = Variable.Value.Hex(loaded.drop(2).joinToString("") { it.toHex().getRawHexStr() }, IKRMini.WORDSIZE).getRawHexStr()

        var paramType: IKRMiniSyntax.ParamType? = null
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { type ->
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

        return StandardAssembler.ResolvedInstr(instrType.name, extString, actualParamType.wordAmount)
    }

    override fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instr? {
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { instrToken.content.uppercase() == it.getDetectionName().uppercase() } ?: return null
        val validParamModes = instrType.paramMap.map { it.key }
        for (amode in validParamModes) {
            val seq = amode.tokenSeq
            if (seq == null) {
                return IKRMiniInstr(instrType, amode, instrToken, listOf(), listOf())
            }

            if (remainingSource.isEmpty()) {
                continue
            }

            val remaining = remainingSource.toMutableList()
            val result = seq.matchStart(*remaining.toTypedArray())
            if (!result.matches) continue
            val expressions = result.nodes.filterIsInstance<GASNode.Expression>()
            return IKRMiniInstr(instrType, amode, instrToken, listOf(),expressions)
        }
        return null
    }
}