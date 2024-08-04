package cengine.ast.node

import cengine.psi.core.TextRange

data class FunctionCall(
    val function: Expression,
    val arguments: List<Expression>,
    override var textRange: TextRange
): Expression(){
    init {
        children.add(function)
        children.addAll(arguments)
    }
}
