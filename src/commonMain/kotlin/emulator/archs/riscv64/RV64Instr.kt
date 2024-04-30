package emulator.archs.riscv64

import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.types.Variable

class RV64Instr(val instrType: RV64Syntax.InstrType, val paramType: RV64Syntax.ParamType, nameToken: Token,allTokens: List<Token>, nodes: List<Node>) : GASNode.Instr(nameToken, allTokens, nodes) {

    val registers = allTokens.mapNotNull { it.reg }
    val expressions = nodes.filterIsInstance<Expression>()

    init {
        expressions.joinToString { it::class.simpleName.toString() }
    }

    override fun getWidth(): Variable.Size {
        TODO("Not yet implemented")
    }

}