package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.nativeError
import emulator.kit.types.Variable

class IKRMiniInstr(val type: IKRMiniSyntax.InstrType, val validAModes: List<IKRMiniSyntax.ParamType>, instrName: Token, allTokens: List<Token>, nodes: List<Node>) : GASNode.Instruction(instrName, allTokens, nodes) {

    val numericExprs = nodes.filterIsInstance<NumericExpr>()
    var addressMode: IKRMiniSyntax.ParamType? = null
    override fun getWidth(): Variable.Size {
        TODO("Not yet implemented")
    }

    fun getOpBin(arch: Architecture): Array<Variable.Value.Bin> {
        val currentAMode = addressMode
        if(currentAMode == null){
            nativeError("ParamType not yet specified!")
            return arrayOf()
        }
        val opCode = type.paramMap[currentAMode]
        val addr = addr
        val expression = numericExprs.firstOrNull()
        if (opCode == null) {
            arch.getConsole().error("Couldn't resolve opcode for the following combination: ${type.name} and ${currentAMode.name}")
            return emptyArray()
        }
        val opCodeArray = mutableListOf(*opCode.splitToByteArray().map { it.toBin() }.toTypedArray())

        when (currentAMode) {
            IKRMiniSyntax.ParamType.IMMEDIATE, IKRMiniSyntax.ParamType.DIRECT, IKRMiniSyntax.ParamType.INDIRECT -> {
                if (expression == null) {
                    arch.getConsole().error("Missing imm/address for the following combination: ${type.name} and ${currentAMode.name}")
                    return emptyArray()
                }
                opCodeArray.addAll(expression.getValue(IKRMini.WORDSIZE).toHex().splitToByteArray().map { it.toBin() })
            }

            IKRMiniSyntax.ParamType.DESTINATION -> {
                if (expression == null) {
                    arch.getConsole().error("Missing branch destination for the following combination: ${type.name} and ${currentAMode.name}")
                    return emptyArray()
                }
                if (addr == null) {
                    arch.getConsole().error("Missing instruction address for the following combination: ${type.name} and ${currentAMode.name}")
                    return emptyArray()
                }
                opCodeArray.addAll(expression.getValue(IKRMini.WORDSIZE).toHex().splitToByteArray().map { it.toBin() })
            }

            IKRMiniSyntax.ParamType.IMPLIED -> {}
        }
        return opCodeArray.toTypedArray()
    }
}