package cengine.ast.node

import cengine.psi.core.TextRange

data class Identifier(
    val name: String,
    override var textRange: TextRange
): Expression(){
    override val children: List<Node> = emptyList()
}
