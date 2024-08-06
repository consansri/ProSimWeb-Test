package cengine.ast.node



data class FunctionDefinition(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: TypeNode?,
    val body: BlockStatement,
    override var range: IntRange
) : Node() {

    init {
        children.addAll(parameters + listOfNotNull(returnType, body))
    }
}
