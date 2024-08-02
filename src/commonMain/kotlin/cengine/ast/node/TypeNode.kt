package cengine.ast.node

import cengine.psi.core.TextRange

data class TypeNode(
    val name: String,
    val genericParameters: List<TypeNode> = emptyList(),
    override var textRange: TextRange
): Node() {
    override val children: List<Node> = genericParameters
}
