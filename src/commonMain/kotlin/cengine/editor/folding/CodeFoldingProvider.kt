package cengine.editor.folding

import cengine.psi.core.PsiFile

interface CodeFoldingProvider {
    var cachedFoldRegions: List<FoldRegion>
    fun getFoldingRegions(psiFile: PsiFile): List<FoldRegion>
    fun getVisibleLines(totalLines: Int): List<Int> {
        val visibleLines = mutableListOf<Int>()
        var curr = 1
        while (curr <= totalLines) {
            if (cachedFoldRegions.firstOrNull { it.foldedRange.contains(curr) } == null) {
                visibleLines.add(curr)
            }
            curr++
        }
        //nativeLog("Return visible lines: ${visibleLines.joinToString()}")
        return visibleLines
    }

}