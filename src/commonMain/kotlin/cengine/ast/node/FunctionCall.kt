package cengine.ast.node

data class FunctionCall(
    val function: Expression,
    val arguments: List<Expression>,
    override var range: IntRange
): Expression(){
    init {
        children.add(function)
        children.addAll(arguments)
    }
}
