package cengine.ast.node

import cengine.psi.core.TextRange

data class TypeNode(
    val name: String,
    val genericParameters: List<TypeNode> = emptyList(),
    override val range: TextRange
): ASTNode() {
    override val children: List<ASTNode> = genericParameters
}
