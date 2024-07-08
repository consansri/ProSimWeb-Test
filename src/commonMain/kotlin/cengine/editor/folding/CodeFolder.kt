package cengine.editor.folding

import cengine.editor.Clearable

class CodeFolder: Clearable {
    private val foldRegions = mutableListOf<FoldRegion>()

    override fun clear(){
        foldRegions.clear()
    }

    fun addFoldRegion(startLine: Int, endLine: Int) {
        foldRegions.add(FoldRegion(startLine, endLine, false))
    }

    fun toggleFold(line: Int) {
        foldRegions.find { it.startLine == line }?.let { it.isFolded = !it.isFolded }
    }

    fun getVisibleLines(totalLines: Int): List<Int> {
        val visibleLines = mutableListOf<Int>()
        var curr = 1
        while (curr <= totalLines) {
            if (foldRegions.firstOrNull { it.foldedRange.contains(curr) } == null) {
                visibleLines.add(curr)
            }
            curr++
        }
        //nativeLog("Return visible lines: ${visibleLines.joinToString()}")
        return visibleLines
    }
}