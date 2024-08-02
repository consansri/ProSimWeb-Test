package cengine.ast.node

import cengine.psi.core.TextRange

data class VariableDeclaration(
    val name: String,
    val type: TypeNode?,
    val initializer: Expression?,
    override var textRange: TextRange
) : Node() {
    override val children: List<Node> = listOfNotNull(type, initializer)
}
