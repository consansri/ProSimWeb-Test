package emulator.archs.ikrmini

import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.syntax.Rule
import emulator.kit.memory.Memory
import emulator.kit.optional.Feature
import emulator.core.*
import emulator.core.Size.Bit16
import emulator.core.Value.Bin
import emulator.core.Value.Hex

object IKRMiniAssembler : AsmHeader {
    override val memAddrSize: Size = IKRMini.MEM_ADDRESS_WIDTH
    override val wordSize: Size = IKRMini.WORDSIZE
    override val detectRegistersByName: Boolean = false
    override val addrShift: Int = 0
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""^[a-zA-Z._][a-zA-Z0-9._]*""")
    }

    override fun instrTypes(features: List<Feature>): List<InstrTypeInterface> = IKRMiniSyntax.InstrType.entries

    override fun additionalDirectives(): List<DirTypeInterface> = listOf()

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val instrType = IKRMiniSyntax.InstrType.entries.firstOrNull { rawInstr.instrName.content.uppercase() == it.getDetectionName().uppercase() } ?: throw Parser.ParserError(rawInstr.instrName, "Is not a IKRMini Instruction!")
        val validParamModes = instrType.paramMap.map { it.key }

        var result: Rule.MatchResult
        for (amode in validParamModes) {
            val seq = amode.tokenSeq
            if (seq == null) {
                return listOf(IKRMiniInstr(instrType, amode, rawInstr, listOf()))
            }

            result = seq.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>()

            return listOf(IKRMiniInstr(instrType, amode, rawInstr, expr))
        }
        throw Parser.ParserError(rawInstr.instrName, "Illegal Operands for IKRMini Instruction!")
    }

    class IKRMiniInstr(val type: IKRMiniSyntax.InstrType, val aMode: IKRMiniSyntax.ParamType, val rawInstr: GASNode.RawInstr, val expr: List<GASNode.NumericExpr>) : GASParser.SecContent {
        override val bytesNeeded: Int = aMode.wordAmount * 2
        val calculatedImm: MutableList<Value> = mutableListOf()
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun allTokensIncludingPseudo(): List<Token> = rawInstr.tokensIncludingReferences()
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM

        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            val currentAMode = aMode

            val opCode = type.paramMap[currentAMode]

            if (opCode == null) {
                throw Parser.ParserError(rawInstr.instrName, "Couldn't resolve opcode for the following combination: ${type.name} and ${currentAMode.name}")
            }
            val opCodeArray = mutableListOf(opCode.toBin())

            expr.forEach { it.assignLabels(labels) }

            when (currentAMode) {
                IKRMiniSyntax.ParamType.INDIRECT_WITH_OFFSET -> {
                    val offset = expr.getOrNull(0)?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Missing Offset!")
                    val address = expr.getOrNull(1)?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Missing Address!")
                    if (!offset.check(Bit16).valid) throw Parser.ParserError(expr.first().tokens().first(), "Offset exceeds 16 Bits!")
                    if (!address.toBin().toUDec().check(Bit16).valid && !address.check(Bit16).valid) throw Parser.ParserError(expr.first().tokens().first(), "Address exceeds 16 Bits!")
                    val offset16 = offset.getResized(Bit16)
                    val address16 = address.toBin().getUResized(Bit16).toHex()
                    calculatedImm.add(offset16)
                    calculatedImm.add(address16)

                    opCodeArray.add(offset16.toBin())
                    opCodeArray.add(address16.toBin())
                }

                IKRMiniSyntax.ParamType.IMMEDIATE, IKRMiniSyntax.ParamType.DIRECT, IKRMiniSyntax.ParamType.INDIRECT -> {
                    val imm = expr.firstOrNull()?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Missing Numeric Expression!")
                    val sizematches = if (currentAMode == IKRMiniSyntax.ParamType.IMMEDIATE) {
                        imm.check(Bit16).valid || imm.toBin().checkSizeUnsigned(Bit16) == null
                    } else {
                        imm.toBin().checkSizeUnsigned(Bit16) == null
                    }
                    if (!sizematches) throw Parser.ParserError(expr.first().tokens().first(), "Expression exceeds 16 Bits!")
                    val ext = imm.toBin().getUResized(Bit16)
                    calculatedImm.add(ext.toHex())

                    opCodeArray.add(ext)
                }

                IKRMiniSyntax.ParamType.DESTINATION -> {
                    val imm = expr.first().evaluate(true)
                    val sizematches = imm.toBin().checkSizeUnsigned(Bit16) == null

                    if (!sizematches) throw Parser.ParserError(expr.first().tokens().first(), "Expression exceeds 16 Bits!")
                    val rel = (imm.toBin().getUResized(Bit16) - yourAddr.toBin())
                    calculatedImm.add(rel.toHex())
                    opCodeArray.add(rel.toBin())
                }

                IKRMiniSyntax.ParamType.IMPLIED -> {}

            }
            return opCodeArray.toTypedArray()
        }

        override fun getContentString(): String = "$type ${getParamString()}"

        private fun getParamString(): String {
            val extString: String = when (aMode) {
                IKRMiniSyntax.ParamType.INDIRECT_WITH_OFFSET -> "(${calculatedImm.getOrNull(0)}, ($${calculatedImm.getOrNull(1)?.toRawString()}))"
                IKRMiniSyntax.ParamType.INDIRECT -> "(($${calculatedImm.firstOrNull()?.toRawString()}))"
                IKRMiniSyntax.ParamType.DIRECT -> "($${calculatedImm.firstOrNull()?.toRawString()})"
                IKRMiniSyntax.ParamType.IMMEDIATE -> "#$${calculatedImm.firstOrNull()?.toRawString()}"
                IKRMiniSyntax.ParamType.DESTINATION -> "$${calculatedImm.firstOrNull()?.toRawString()}"
                IKRMiniSyntax.ParamType.IMPLIED -> ""
            }

            return extString
        }
    }
}