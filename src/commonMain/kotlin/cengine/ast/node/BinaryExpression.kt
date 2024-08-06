package cengine.ast.node

data class BinaryExpression(
    val left: Expression,
    val operator: String,
    val right: Expression,
    override var range: IntRange
): Expression(){
    override val children: MutableList<Node> = mutableListOf(left, right)
}
