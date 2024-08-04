package cengine.ast.node

import cengine.psi.core.TextRange

data class Literal(
    val value: String,
    val type: String,
    override var textRange: TextRange
): Expression(){
}
