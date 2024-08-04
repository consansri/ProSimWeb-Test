package cengine.ast.node

import cengine.psi.core.TextRange

class BlockStatement(
    statements: List<Statement>,
    override var textRange: TextRange
): Node(){
    init {
        children.addAll(statements)
    }
}
