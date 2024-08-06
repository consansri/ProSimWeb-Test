package cengine.ast.node

class BlockStatement(
    statements: List<Statement>,
    override var range: IntRange
): Node(){
    init {
        children.addAll(statements)
    }
}
