package emulator.archs.t6502

import emulator.archs.ikrmini.IKRMini
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.Memory
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

class T6502Assembler() : DefinedAssembly {

    override val INSTRS_ARE_WORD_ALIGNED: Boolean = false
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = InstrType.entries
    override fun getAdditionalDirectives(): List<DirTypeInterface> = listOf()
    override val detectRegistersByName: Boolean = false
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
    }
    override val MEM_ADDRESS_SIZE: Variable.Size = T6502.MEM_ADDR_SIZE
    override val WORD_SIZE: Variable.Size = T6502.WORD_SIZE



    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        val instrType = InstrType.entries.firstOrNull { rawInstr.instrName.instr?.getDetectionName() == it.name } ?: throw Parser.ParserError(rawInstr.instrName, "Is not a T6502 Instruction!")
        val possibleAModes = instrType.opCode.map { it.key }

        var result: Rule.MatchResult

        for (amode in possibleAModes) {
            val seq = amode.tokenSequence
            if (seq == null) {
                return listOf(T6502Instr(instrType, amode, rawInstr))
            }

            result = seq.matchStart(rawInstr.remainingTokens, listOf(), this, tempContainer.symbols)
            if (!result.matches) continue

            val expr = result.matchingNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()

            val immediate = expr?.evaluate(false)
            val immSize = if (amode.byteAmount == 2) {
                Variable.Size.Bit8()
            } else {
                Variable.Size.Bit16()
            }

            val resized = when (immediate) {
                is Dec -> {
                    if (!immediate.check(immSize).valid) continue
                    immediate.getResized(IKRMini.WORDSIZE).toHex()
                }

                else -> {
                    if (immediate?.check(immSize)?.valid == false) continue
                    immediate?.toBin()?.getUResized(IKRMini.WORDSIZE)?.toHex()
                }
            }

            return listOf(T6502Instr(instrType, amode, rawInstr, resized))
        }

        throw Parser.ParserError(rawInstr.instrName, "Instruction has illegal operands!")
    }

    class T6502Instr(val type: InstrType, val amode: AModes, val rawInstr: GASNode.RawInstr, immediate: Hex? = null) : GASParser.SecContent {
        override val bytesNeeded: Int = amode.byteAmount
        val immediate: Hex
        init {
            this.immediate = immediate ?: Hex("0", Variable.Size.Bit8())
        }
        override fun getFirstToken(): Token = rawInstr.instrName
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<GASParser.Label, Hex>>): Array<Bin> {
            val opCode = type.opCode[amode]
            if (opCode == null) {
                throw Parser.ParserError(rawInstr.instrName, "Couldn't resolve opcode for the following combination: ${type.name} and ${amode.name}")
            }

            val codeWithExt: Array<Bin> = when (amode) {
                AModes.ZP_X, AModes.ZP_Y, AModes.ZPIND_Y, AModes.ZP_X_IND, AModes.ZP, AModes.IMM -> {
                    arrayOf(opCode.toBin(), immediate.toBin())
                }

                AModes.ABS_X, AModes.ABS_Y, AModes.ABS, AModes.IND -> {
                    arrayOf(opCode.toBin(), *immediate.splitToByteArray().map { it.toBin() }.toTypedArray())
                }

                AModes.ACCUMULATOR, AModes.IMPLIED -> arrayOf(opCode.toBin())

                AModes.REL -> {
                    arrayOf(opCode.toBin(), immediate.toBin())
                }
            }
            return codeWithExt
        }

        override fun getContentString(): String = "$type ${amode.getString(immediate)}"
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