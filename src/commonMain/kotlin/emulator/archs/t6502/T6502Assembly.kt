package emulator.archs.t6502

import emulator.archs.ikrmini.IKRMiniSyntax
import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

class T6502Assembly() : DefinedAssembly {

    override val INSTRS_ARE_WORD_ALIGNED: Boolean = false
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = InstrType.entries
    override fun getAdditionalDirectives(): List<DirTypeInterface> = listOf()
    override val detectRegistersByName: Boolean = false
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes{
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
    }
    override val MEM_ADDRESS_SIZE: Variable.Size = T6502.MEM_ADDR_SIZE
    override val WORD_SIZE: Variable.Size = T6502.WORD_SIZE

    override fun getInstrSpace(arch: Architecture, instr: GASNode.Instr): Int {
        if (instr !is T6502Instr) {
            arch.getConsole().error("Expected [${T6502Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        val t6502Instr: T6502Instr = instr
        return t6502Instr.addressingMode.byteAmount
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: GASNode.Instr): Array<Bin> {
        if (instr !is T6502Instr) {
            arch.getConsole().error("Expected [${T6502Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        val t6502Instr: T6502Instr = instr
        return t6502Instr.getOpBin(arch).map { it.toBin() }.toTypedArray()
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Hex): StandardAssembler.ResolvedInstr? {
        val threeByte = arch.getMemory().loadArray(currentAddress, 3)

        var paramType: AModes? = null
        val instrType = InstrType.entries.firstOrNull { type ->
            paramType = type.opCode.entries.firstOrNull { threeByte.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase() }?.key
            paramType != null
        }
        val actualParamType = paramType
        if (actualParamType == null || instrType == null) {
            return null
        }
        val ext: String = actualParamType.getString(threeByte)
        return StandardAssembler.ResolvedInstr(instrType.name, ext, actualParamType.byteAmount)
    }

    override fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instr? {
        val instrType = InstrType.entries.firstOrNull { instrToken.instr?.getDetectionName() == it.name } ?: return null
        val validParamModes = instrType.opCode.map { it.key }
        for (amode in validParamModes) {
            val seq = amode.tokenSequence
            if (seq == null) {
                return T6502Instr(instrType, amode, instrToken, listOf(), listOf())
            }

            if (remainingSource.isEmpty()) {
                continue
            }

            val remaining = remainingSource.toMutableList()
            val result = seq.matchStart(*remaining.toTypedArray())
            if (!result.matches) continue
            val allTokens = result.sequenceMap.flatMap { it.token.toList() }
            return T6502Instr(instrType, amode, instrToken, allTokens, result.nodes)
        }
        return null
    }

    data class Flags(
        val n: Bin,
        val v: Bin,
        val z: Bin,
        val c: Bin,
        val d: Bin,
        val b: Bin,
        val i: Bin,
    )

}