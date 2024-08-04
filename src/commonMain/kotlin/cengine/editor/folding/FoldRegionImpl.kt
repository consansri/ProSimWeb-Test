package cengine.editor.folding

/**
 * @param startLine 0..<lines
 * @param endLine 0..<lines
 * @param isFolded state of folding
 */
data class FoldRegionImpl(
    override val startLine: Int,
    override val endLine: Int,
    override var isFolded: Boolean,
    override val placeholder: String
) : FoldRegion {

    override fun isExpanded(): Boolean = !isFolded

    override fun setExpanded(expanded: Boolean) {
        isFolded = !expanded
    }

}
