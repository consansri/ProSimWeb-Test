package cengine.ast.node



data class Parameter(
    val name: String,
    val type: TypeNode,
    override var range: IntRange
): Node() {
    init {
        children.add(type)
    }
}
