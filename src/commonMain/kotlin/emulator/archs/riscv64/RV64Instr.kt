package emulator.archs.riscv64

import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import emulator.kit.types.Variable

class RV64Instr(val instrType: RV64Syntax.InstrType, val paramType: RV64Syntax.ParamType, nameToken: Token.KEYWORD.InstrName,allTokens: List<Token>, nodes: List<Node>) : GASNode.Instr(nameToken, allTokens, nodes) {

    val registers = allTokens.filterIsInstance<Token.KEYWORD.Register>()
    val expressions = nodes.filterIsInstance<Expression>()

    init {
        expressions.joinToString { it::class.simpleName.toString() }
    }

    override fun getWidth(): Variable.Size {
        TODO("Not yet implemented")
    }

}