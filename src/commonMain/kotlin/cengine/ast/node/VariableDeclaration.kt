package cengine.ast.node


data class VariableDeclaration(
    val name: String,
    val type: TypeNode?,
    val initializer: Expression?,
    override var range: IntRange
) : Node() {

    init {
        children.addAll(listOfNotNull(type, initializer))
    }
}
