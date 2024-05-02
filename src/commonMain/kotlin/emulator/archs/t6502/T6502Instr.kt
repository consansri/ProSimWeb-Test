package emulator.archs.t6502

import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.types.Variable
import emulator.archs.t6502.AModes.*
import emulator.kit.assembler.parser.Node
import emulator.kit.nativeError

class T6502Instr(
    val type: InstrType,
    val validAModes: List<AModes>,
    nameToken: Token,
    allTokens: List<Token>,
    nodes: List<Node>
) : GASNode.Instruction(
    nameToken,
    allTokens,
    nodes
) {
    var addressMode: AModes? = null
    val numericExpr = nodes.firstOrNull{it is NumericExpr} as NumericExpr?

    fun getOpBin(arch: emulator.kit.Architecture): Array<Variable.Value.Hex> {
        val currAMode = addressMode
        if(currAMode == null){
            nativeError("ParamType not yet specified!")
            return arrayOf()
        }

        val opCode = type.opCode[currAMode]
        val addr = addr
        if (opCode == null) {
            arch.getConsole().error("Couldn't resolve opcode for the following combination: ${type.name} and ${currAMode.name}")
            return emptyArray()
        }

        val codeWithExt: Array<Variable.Value.Hex> = when (currAMode) {
            ZP_X, ZP_Y, ZPIND_Y, ZP_X_IND, ZP, IMM -> {
                if (numericExpr == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${currAMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, numericExpr.getValue(T6502.BYTE_SIZE).toHex())
            }

            ABS_X, ABS_Y, ABS, IND -> {
                if (numericExpr == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${currAMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, *numericExpr.getValue(T6502.WORD_SIZE).toHex().splitToByteArray())
            }

            ACCUMULATOR, IMPLIED -> arrayOf(opCode)

            REL -> {
                if (numericExpr == null) {
                    arch.getConsole().error("Missing imm for the following combination: ${type.name} and ${currAMode.name}")
                    return emptyArray()
                }
                arrayOf(opCode, numericExpr.getValue(T6502.BYTE_SIZE).toHex())
            }
        }
        return codeWithExt
    }

    override fun getWidth(): Variable.Size {
        TODO("Not yet implemented")
    }
}