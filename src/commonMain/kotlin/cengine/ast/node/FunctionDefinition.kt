package cengine.ast.node

import cengine.psi.core.TextRange

data class FunctionDefinition(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: TypeNode?,
    val body: BlockStatement,
    override var textRange: TextRange
) : Node() {
    override val children: List<Node> = parameters + listOfNotNull(returnType, body)
}
