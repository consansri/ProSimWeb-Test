package emulator.archs.riscv64

import emulator.archs.riscv32.RV32Syntax
import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.gas.riscv.GASRVDirType
import emulator.kit.compiler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class RV64Assembler: DefinedAssembly {
    private val binMapper = RV64BinMapper()

    override val MEM_ADDRESS_SIZE: Variable.Size = RV64.XLEN
    override val WORD_SIZE: Variable.Size = RV64.WORD_WIDTH
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean  = true
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes{
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
        val instrList = RV64Syntax.InstrType.entries.toMutableList()

        for(type in ArrayList(instrList)){
            type.needFeatures.forEach { featureID ->
                val featureIsActive = features.firstOrNull { it.id == featureID }?.isActive()
                if (featureIsActive == false) {
                    instrList.remove(type)
                    return@forEach
                }
            }
        }

        return instrList
    }

    override fun getAdditionalDirectives(): List<DirTypeInterface> = GASRVDirType.entries

    override fun getInstrSpace(arch: Architecture, instr: GASNode.Instr): Int {
        if (instr !is RV64Instr) {
            arch.getConsole().error("Expected [${RV64Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        return instr.instrType.memWords
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: GASNode.Instr): Array<Variable.Value.Bin> {
        if (instr !is RV64Instr) {
            arch.getConsole().error("Expected [${RV64Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        return binMapper.getBinaryFromInstrDef(instr, arch)
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr? {
        val instr = binMapper.getInstrFromBinary(arch.getMemory().load(currentAddress, 4).toBin()) ?: return null
        return StandardAssembler.ResolvedInstr(instr.type.id, instr.type.paramType.getTSParamString(arch, instr.binMap.toMutableMap()), instr.type.memWords)
    }

    override fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instr? {
        val types = RV64Syntax.InstrType.entries.filter { it.getDetectionName() == instrToken.instr?.getDetectionName() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.tokenSeq?.matchStart(*remainingSource.toTypedArray()) ?: return RV64Instr(type, paramType, instrToken, listOf(), listOf())
            if (!result.matches) continue

            val allTokens = result.sequenceMap.flatMap { it.token.toList() }
            return RV64Instr(type, paramType, instrToken, allTokens,result.nodes)
        }

        return null
    }

}