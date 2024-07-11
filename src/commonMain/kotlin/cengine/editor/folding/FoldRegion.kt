package cengine.editor.folding

/**
 * @param startLine 0..<lines
 * @param endLine 0..<lines
 * @param isFolded state of folding
 */
data class FoldRegion(
    val startLine: Int,
    val endLine: Int,
    var isFolded: Boolean
) {
    val foldedRange = IntRange(startLine + 1, endLine)
}
