package emulator.archs.ikrmini

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.Memory
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class IKRMiniAssembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size = IKRMini.MEM_ADDRESS_WIDTH
    override val WORD_SIZE: Variable.Size = IKRMini.WORDSIZE
    override val INSTRS_ARE_WORD_ALIGNED: Boolean = true
    override val detectRegistersByName: Boolean = false
    override val numberPrefixes: DefinedAssembly.NumberPrefixes = object : DefinedAssembly.NumberPrefixes {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = IKRMiniSyntax.InstrType.entries

    override fun getAdditionalDirectives(): List<DirTypeInterface> = listOf()



    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { rawInstr.instrName.content.uppercase() == it.getDetectionName().uppercase() } ?: throw Parser.ParserError(rawInstr.instrName, "Is not a IKRMini Instruction!")
        val validParamModes = instrType.paramMap.map { it.key }

        var result: Rule.MatchResult
        for (amode in validParamModes) {
            val seq = amode.tokenSeq
            if (seq == null) {
                return listOf(IKRMiniInstr(instrType, amode, rawInstr,null))
            }

            result = seq.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()

            val immediate = expr?.evaluate(false)
            val lblExpr = if(expr != null && !expr.isDefined()) expr else null
            val resized = when (immediate) {
                is Variable.Value.Dec -> {
                    immediate.getResized(IKRMini.WORDSIZE).toHex()
                }

                else -> {
                    immediate?.toBin()?.getUResized(IKRMini.WORDSIZE)?.toHex()
                }
            }

            return listOf(IKRMiniInstr(instrType, amode, rawInstr, resized, lblExpr))
        }
        throw Parser.ParserError(rawInstr.instrName, "Illegal Operands for IKRMini Instruction!")
    }

    class IKRMiniInstr(val type: IKRMiniSyntax.InstrType, val aMode: IKRMiniSyntax.ParamType, val rawInstr: GASNode.RawInstr, immediate: Variable.Value.Hex?, val label: GASNode.NumericExpr? = null ) : GASParser.SecContent {
        override val bytesNeeded: Int = aMode.wordAmount * 2
        val immediate: Variable.Value.Hex
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        init {
            this.immediate = immediate ?: Variable.Value.Hex("0", Variable.Size.Bit16())
            // TODO(Handle Labels)
        }

        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Variable.Value.Hex>>): Array<Variable.Value.Bin> {
            val currentAMode = aMode

            val opCode = type.paramMap[currentAMode]
            val addr = yourAddr

            if (opCode == null) {
                throw Parser.ParserError(rawInstr.instrName, "Couldn't resolve opcode for the following combination: ${type.name} and ${currentAMode.name}")
            }
            val opCodeArray = mutableListOf(*opCode.splitToByteArray().map { it.toBin() }.toTypedArray())

            when (currentAMode) {
                IKRMiniSyntax.ParamType.IMMEDIATE, IKRMiniSyntax.ParamType.DIRECT, IKRMiniSyntax.ParamType.INDIRECT -> {
                    opCodeArray.addAll(immediate.splitToByteArray().map { it.toBin() })
                }

                IKRMiniSyntax.ParamType.DESTINATION -> {
                    opCodeArray.addAll(immediate.splitToByteArray().map { it.toBin() })
                }

                IKRMiniSyntax.ParamType.IMPLIED -> {}
            }
            return opCodeArray.toTypedArray()
        }

        override fun getContentString(): String = "$type $aMode ${getParamString()}"

        fun getParamString(): String {
            val extString: String = when (aMode) {
                IKRMiniSyntax.ParamType.INDIRECT -> "(($${immediate}))"
                IKRMiniSyntax.ParamType.DIRECT -> "($${immediate})"
                IKRMiniSyntax.ParamType.IMMEDIATE -> "#$${immediate}"
                IKRMiniSyntax.ParamType.DESTINATION -> "$${immediate}"
                IKRMiniSyntax.ParamType.IMPLIED -> ""
            }

            return extString
        }
    }
}