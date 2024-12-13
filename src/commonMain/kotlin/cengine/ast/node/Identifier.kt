package cengine.ast.node



data class Identifier(
    val name: String,
    override var range: IntRange
): Expression()
