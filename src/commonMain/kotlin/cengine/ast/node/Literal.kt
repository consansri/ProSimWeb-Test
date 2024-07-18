package cengine.ast.node

import cengine.psi.core.TextRange

data class Literal(
    val value: String,
    val type: String,
    override val range: TextRange
): Expression(){
    override val children: List<ASTNode> = emptyList()
}
