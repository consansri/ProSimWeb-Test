package cengine.editor.folding

interface FoldRegion {

    val startLine: Int
    val endLine: Int
    var isFolded: Boolean
    val placeholder: String

    val foldRange: IntRange get() = IntRange(startLine + 1, endLine)

    companion object {
        val EMPTY_ARRAY: Array<FoldRegion> = arrayOf()
    }

    fun isExpanded(): Boolean

    fun setExpanded(expanded: Boolean)

}