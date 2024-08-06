package cengine.ast.node



class Program(blockStatements: List<Node>, override var range: IntRange): Node(){
    init {
        children.addAll(blockStatements)
    }

}
