package cengine.ast.node

import cengine.psi.core.TextRange

data class Identifier(
    val name: String,
    override val range: TextRange
): Expression(){
    override val children: List<ASTNode> = emptyList()
}
