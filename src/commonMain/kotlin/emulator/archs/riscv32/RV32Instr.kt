package emulator.archs.riscv32

import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.types.Variable

class RV32Instr(val instrType: RV32Syntax.InstrType, val paramType: RV32Syntax.ParamType, nameToken: Token, allTokens: List<Token>, nodes: List<Node>) : GASNode.Instr(nameToken, allTokens, nodes){

    val registers = allTokens.mapNotNull { it.reg }
    val numericExprs = nodes.filterIsInstance<NumericExpr>()
    override fun getWidth(): Variable.Size {
        TODO()
    }

}