package emulator.archs.t6502

import emulator.archs.ikrmini.IKRMini
import emulator.kit.assembler.DefinedAssembly
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.memory.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.Bit16
import emulator.kit.types.Variable.Size.Bit8
import emulator.kit.types.Variable.Value.*

class T6502Assembler : DefinedAssembly {
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = InstrType.entries
    override fun getAdditionalDirectives(): List<DirTypeInterface> = listOf()
    override val detectRegistersByName: Boolean = false
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }
    override val memAddrSize: Variable.Size = T6502.MEM_ADDR_SIZE
    override val wordSize: Variable.Size = T6502.WORD_SIZE

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val instrType = InstrType.entries.firstOrNull { rawInstr.instrName.instr?.getDetectionName() == it.name } ?: throw Parser.ParserError(rawInstr.instrName, "Is not a T6502 Instruction!")
        val possibleAModes = AModes.entries.filter { instrType.opCode.map { it.key }.contains(it) }

        var result: Rule.MatchResult

        for (amode in possibleAModes) {
            val seq = amode.tokenSequence
            if (seq == null) {
                return listOf(T6502Instr(instrType, amode, rawInstr))
            }

            result = seq.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()

            val immediate = try {
                expr?.evaluate(true)
            } catch (e: Parser.ParserError) {
                null
            }

            val immSize = if (amode.byteAmount == 2) {
                Bit8()
            } else {
                Bit16()
            }

            val resized = when (immediate) {
                is Dec -> {
                    if (!immediate.check(immSize).valid) continue
                    immediate.getResized(IKRMini.WORDSIZE).toHex()
                }

                null -> {
                    null
                }

                else -> {
                    if (immediate.check(immSize).valid == false) continue
                    immediate.toBin().getUResized(IKRMini.WORDSIZE).toHex()
                }
            }

            return listOf(T6502Instr(instrType, amode, rawInstr, expr, resized))
        }

        throw Parser.ParserError(rawInstr.instrName, "Instruction has illegal operands!")
    }

    class T6502Instr(val type: InstrType, val amode: AModes, val rawInstr: GASNode.RawInstr, val expr: GASNode.NumericExpr? = null, var immediate: Hex? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = amode.byteAmount

        init {
            nativeLog("Found 6502Instr: $type $amode expr:$expr imm:$immediate")
        }

        override fun getFirstToken(): Token = rawInstr.instrName
        override fun allTokensIncludingPseudo(): List<Token> = rawInstr.tokensIncludingReferences()

        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            val opCode = type.opCode[amode]
            if (opCode == null) {
                throw Parser.ParserError(rawInstr.instrName, "Couldn't resolve opcode for the following combination: ${type.name} and ${amode.name}")
            }

            expr?.assignLabels(labels)

            val codeWithExt: Array<Bin> = when (amode) {
                AModes.ZP_X, AModes.ZP_Y, AModes.ZPIND_Y, AModes.ZP_X_IND, AModes.ZP, AModes.IMM -> {
                    val imm = immediate ?: expr?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Expression is missing!")
                    immediate = imm.toHex()
                    arrayOf(opCode.toBin(), imm.toBin())
                }

                AModes.ABS_X, AModes.ABS_Y, AModes.ABS, AModes.IND -> {
                    val imm = immediate ?: expr?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Expression is missing!")
                    immediate = imm.toHex()
                    arrayOf(opCode.toBin(), *imm.toBin().splitToByteArray().map { it.toBin() }.toTypedArray())
                }

                AModes.ACCUMULATOR, AModes.IMPLIED -> arrayOf(opCode.toBin())

                AModes.REL -> {
                    val imm = immediate ?: expr?.evaluate(true) ?: throw Parser.ParserError(rawInstr.instrName, "Expression is missing!")
                    immediate = imm.toHex()
                    arrayOf(opCode.toBin(), (imm.toBin() - yourAddr.toBin()).toBin())
                }
            }
            return codeWithExt
        }

        override fun getContentString(): String = "$type ${amode.getString(immediate ?: Hex("0", Bit8()))}"
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