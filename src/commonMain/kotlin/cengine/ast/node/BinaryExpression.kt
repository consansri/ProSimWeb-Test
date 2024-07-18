package cengine.ast.node

import cengine.psi.core.TextRange

data class BinaryExpression(
    val left: Expression,
    val operator: String,
    val right: Expression,
    override val range: TextRange
): Expression(){
    override val children: List<ASTNode> = listOf(left, right)
}
