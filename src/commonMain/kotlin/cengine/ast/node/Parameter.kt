package cengine.ast.node

import cengine.psi.core.TextRange

data class Parameter(
    val name: String,
    val type: TypeNode,
    override val range: TextRange
): ASTNode() {
    override val children: List<ASTNode> = listOf(type)
}
