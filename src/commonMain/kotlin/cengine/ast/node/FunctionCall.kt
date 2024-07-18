package cengine.ast.node

import cengine.psi.core.TextRange

data class FunctionCall(
    val function: Expression,
    val arguments: List<Expression>,
    override val range: TextRange
): Expression(){
    override val children: List<ASTNode> = listOf(function) + arguments
}
