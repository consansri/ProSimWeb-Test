package cengine.ast.node

import cengine.psi.core.TextRange

data class VariableDeclaration(
    val name: String,
    val type: TypeNode?,
    val initializer: Expression?,
    override var textRange: TextRange
) : Node() {

    init {
        children.addAll(listOfNotNull(type, initializer))
    }
}
