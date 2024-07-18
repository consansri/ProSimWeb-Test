package cengine.ast.node

import cengine.psi.core.TextRange

data class VariableDeclaration(
    val name: String,
    val type: TypeNode?,
    val initializer: Expression?,
    override val range: TextRange
) : ASTNode() {
    override val children: List<ASTNode> = listOfNotNull(type, initializer)
}
