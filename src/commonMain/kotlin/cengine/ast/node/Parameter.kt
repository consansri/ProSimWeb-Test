package cengine.ast.node

import cengine.psi.core.TextRange

data class Parameter(
    val name: String,
    val type: TypeNode,
    override var textRange: TextRange
): Node() {
    init {
        children.add(type)
    }
}
