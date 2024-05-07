package emulator.archs.ikrmini

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Lexer
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
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""^[a-zA-Z._][a-zA-Z0-9._]*""")
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
                return listOf(IKRMiniInstr(instrType, amode, rawInstr, null))
            }

            result = seq.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()

            val lblExpr = if (expr != null && !expr.isDefined()) expr else null

            return listOf(IKRMiniInstr(instrType, amode, rawInstr, expr, lblExpr))
        }
        throw Parser.ParserError(rawInstr.instrName, "Illegal Operands for IKRMini Instruction!")
    }

    class IKRMiniInstr(val type: IKRMiniSyntax.InstrType, val aMode: IKRMiniSyntax.ParamType, val rawInstr: GASNode.RawInstr, val expr: GASNode.NumericExpr?, val label: GASNode.NumericExpr? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = aMode.wordAmount * 2
        var calculatedImm: Variable.Value.Hex? = null
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM

        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Variable.Value.Hex>>): Array<Variable.Value.Bin> {
            val currentAMode = aMode

            val opCode = type.paramMap[currentAMode]
            val addr = yourAddr

            if (opCode == null) {
                throw Parser.ParserError(rawInstr.instrName, "Couldn't resolve opcode for the following combination: ${type.name} and ${currentAMode.name}")
            }
            val opCodeArray = mutableListOf(*opCode.splitToByteArray().map { it.toBin() }.toTypedArray())

            expr?.assignLabels(labels)

            when (currentAMode) {
                IKRMiniSyntax.ParamType.IMMEDIATE, IKRMiniSyntax.ParamType.DIRECT, IKRMiniSyntax.ParamType.INDIRECT -> {
                    val imm = expr?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Missing Numeric Expression!")
                    val sizematches = if (currentAMode == IKRMiniSyntax.ParamType.IMMEDIATE) {
                        imm.check(Variable.Size.Bit16()).valid || imm.toBin().checkSizeUnsigned(Variable.Size.Bit16()) == null
                    } else {
                        imm.toBin().checkSizeUnsigned(Variable.Size.Bit16()) == null
                    }
                    if (!sizematches) throw Parser.ParserError(expr.getAllTokens().first(), "Expression exceeds 16 Bits!")
                    val ext = imm.toBin().getUResized(Variable.Size.Bit16())
                    calculatedImm = ext.toHex()

                    opCodeArray.addAll(ext.splitToByteArray().map { it.toBin() })
                }

                IKRMiniSyntax.ParamType.DESTINATION -> {
                    val imm = expr?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Missing Numeric Expression!")
                    val sizematches = imm.toBin().checkSizeUnsigned(Variable.Size.Bit16()) == null

                    if (!sizematches) throw Parser.ParserError(expr.getAllTokens().first(), "Expression exceeds 16 Bits!")
                    val rel = (imm.toBin().getUResized(Variable.Size.Bit16()) - yourAddr.toBin())
                    calculatedImm = rel.toHex()
                    opCodeArray.addAll(rel.toBin().splitToByteArray().map { it.toBin() })
                }

                IKRMiniSyntax.ParamType.IMPLIED -> {}
            }
            return opCodeArray.toTypedArray()
        }

        override fun getContentString(): String = "$type $aMode ${getParamString()}"

        fun getParamString(): String {
            val extString: String = when (aMode) {
                IKRMiniSyntax.ParamType.INDIRECT -> "(($${calculatedImm}))"
                IKRMiniSyntax.ParamType.DIRECT -> "($${calculatedImm})"
                IKRMiniSyntax.ParamType.IMMEDIATE -> "#$${calculatedImm}"
                IKRMiniSyntax.ParamType.DESTINATION -> "$${calculatedImm}"
                IKRMiniSyntax.ParamType.IMPLIED -> ""
            }

            return extString
        }
    }
}