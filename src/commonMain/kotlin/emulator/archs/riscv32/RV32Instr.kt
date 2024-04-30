package emulator.archs.riscv32

import emulator.archs.riscv64.RV64Syntax
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import emulator.kit.types.Variable

class RV32Instr(val instrType: RV32Syntax.InstrType, val paramType: RV32Syntax.ParamType, nameToken: Token, allTokens: List<Token>, nodes: List<Node>) : GASNode.Instr(nameToken, allTokens, nodes){

    val registers = allTokens.mapNotNull { it.reg }
    val expressions = nodes.filterIsInstance<Expression>()
    override fun getWidth(): Variable.Size {
        TODO()
    }

}