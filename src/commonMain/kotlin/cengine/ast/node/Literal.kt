package cengine.ast.node



data class Literal(
    val value: String,
    val type: String,
    override var range: IntRange
): Expression()
