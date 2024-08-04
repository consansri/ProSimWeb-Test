package cengine.ast.node

import cengine.psi.core.TextRange

data class BinaryExpression(
    val left: Expression,
    val operator: String,
    val right: Expression,
    override var textRange: TextRange
): Expression(){
    override val children: MutableList<Node> = mutableListOf(left, right)
}
