package cengine.ast.node



data class TypeNode(
    val name: String,
    val genericParameters: List<TypeNode> = emptyList(),
    override var range: IntRange
): Node() {
    init {
        children.addAll(genericParameters)
    }
}
