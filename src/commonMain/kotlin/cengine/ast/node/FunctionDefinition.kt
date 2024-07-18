package cengine.ast.node

import cengine.psi.core.TextRange

data class FunctionDefinition(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: TypeNode?,
    val body: BlockStatement,
    override val range: TextRange
) : ASTNode() {
    override val children: List<ASTNode> = parameters + listOfNotNull(returnType, body)
}
