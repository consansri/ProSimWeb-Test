package cengine.editor.folding

interface FoldRegion {

    companion object{
        val EMPTY_ARRAY: Array<FoldRegion> = arrayOf()
    }


    fun isExpanded(): Boolean

    fun setExpanded(expanded: Boolean)

    fun getPlaceHolderText(): String

}