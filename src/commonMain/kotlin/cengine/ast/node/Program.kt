package cengine.ast.node

import cengine.psi.core.TextRange

class Program(blockStatements: List<Node>, override var textRange: TextRange): Node(){
    init {
        children.addAll(blockStatements)
    }

}
