package emulator.archs.t6502

import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.types.Variable
import emulator.archs.t6502.AModes.*
import emulator.kit.assembler.parser.Node

class T6502Instr(
    val type: InstrType,
    val addressingMode: AModes,
    nameToken: Token,
    allTokens: List<Token>,
    nodes: List<Node>
) : GASNode.Instr(
    nameToken,
    allTokens,
    nodes
) {
    val expression = nodes.firstOrNull{it is Expression} as Expression?

    fun getOpBin(arch: emulator.kit.Architecture): Array<Variable.Value.Hex> {
        val opCode = type.opCode[addressingMode]
        val addr = addr
        if (opCode == null) {
            arch.getConsole().error("Couldn't resolve opcode for the following combination: ${type.name} and ${addressingMode.name}")
            return emptyArray()
        }

        val codeWithExt: Array<Variable.Value.Hex> = when (addressingMode) {
            ZP_X, ZP_Y, ZPIND_Y, ZP_X_IND, ZP, IMM -> {
                if (expression == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, expression.getValue(T6502.BYTE_SIZE).toHex())
            }

            ABS_X, ABS_Y, ABS, IND -> {
                if (expression == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, *expression.getValue(T6502.WORD_SIZE).toHex().splitToByteArray())
            }

            ACCUMULATOR, IMPLIED -> arrayOf(opCode)

            REL -> {
                if (expression == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${addressingMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, expression.getValue(T6502.BYTE_SIZE).toHex())
            }
        }
        return codeWithExt
    }

    override fun getWidth(): Variable.Size {
        TODO("Not yet implemented")
    }
}