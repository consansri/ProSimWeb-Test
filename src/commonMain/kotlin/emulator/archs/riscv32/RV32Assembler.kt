package emulator.archs.riscv32

import emulator.kit.Architecture
import emulator.kit.assembly.standards.StandardAssembler
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.riscv.GASRVDirType
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class RV32Assembler: DefinedAssembly {
    private val binMapper = RV32BinMapper()

    override val MEM_ADDRESS_SIZE: Variable.Size = RV32.MEM_ADDRESS_WIDTH
    override val WORD_SIZE: Variable.Size = RV32.WORD_WIDTH
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = true
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes{
        override val bin: String = "0b"
        override val dec: String = ""
        override val hex: String = "0x"
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
        val instrList = RV32Syntax.InstrType.entries.toMutableList()

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

    override fun getInstrSpace(arch: Architecture, instr: GASNode.Instruction): Int {
        if (instr !is RV32Instr) {
            arch.getConsole().error("Expected [${RV32Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return 1
        }
        return instr.instrType.memWords
    }

    override fun getOpBinFromInstr(arch: Architecture, instr: GASNode.Instruction): Array<Variable.Value.Bin> {
        if (instr !is RV32Instr) {
            arch.getConsole().error("Expected [${RV32Instr::class.simpleName}] but received [${instr::class.simpleName}]!")
            return emptyArray()
        }
        return binMapper.getBinaryFromInstrDef(instr, arch)
    }

    override fun getInstrFromBinary(arch: Architecture, currentAddress: Variable.Value.Hex): StandardAssembler.ResolvedInstr? {
        val instr = binMapper.getInstrFromBinary(arch.getMemory().load(currentAddress, 4).toBin()) ?: return null
        return StandardAssembler.ResolvedInstr(instr.type.id, instr.type.paramType.getTSParamString(arch, instr.binMap.toMutableMap()), instr.type.memWords)
    }

    override fun parseInstrParams(instrToken: Token, remainingSource: List<Token>): GASNode.Instruction? {
        val types = RV32Syntax.InstrType.entries.filter { it.getDetectionName().uppercase() == instrToken.content.uppercase() }

        for (type in types) {
            val paramType = type.paramType
            val result = paramType.tokenSeq?.matchStart(remainingSource, listOf(), this) ?: return RV32Instr(type, paramType, instrToken, listOf(), listOf())
            if (!result.matches) continue

            return RV32Instr(type, paramType, instrToken, result.matchingTokens + result.ignoredSpaces, result.matchingNodes)
        }

        return null
    }


}