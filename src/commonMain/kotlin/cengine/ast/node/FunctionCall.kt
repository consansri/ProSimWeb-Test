package cengine.ast.node

import cengine.psi.core.TextRange

data class FunctionCall(
    val function: Expression,
    val arguments: List<Expression>,
    override var textRange: TextRange
): Expression(){
    override val children: List<Node> = listOf(function) + arguments
}
