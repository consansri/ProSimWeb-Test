package cengine.editor.folding

data class FoldRegion(
    val startLine: Int,
    val endLine: Int,
    var isFolded: Boolean
) {
    val foldedRange = IntRange(startLine + 1, endLine)
}
