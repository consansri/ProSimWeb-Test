package cengine.editor.folding

/**
 * @param startLine 0..<lines
 * @param endLine 0..<lines
 * @param isFolded state of folding
 */
data class FoldRegionImpl(
    val startLine: Int,
    val endLine: Int,
    var isFolded: Boolean,
    val placeholder: String
) {
    val foldedRange = IntRange(startLine + 1, endLine)
}
