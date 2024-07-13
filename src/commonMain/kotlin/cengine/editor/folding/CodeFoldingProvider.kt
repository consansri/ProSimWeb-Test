package cengine.editor.folding

import cengine.psi.core.PsiFile

interface CodeFoldingProvider {
    var cachedFoldRegions: List<FoldRegion>
    fun getFoldingRegions(psiFile: PsiFile): List<FoldRegion>

    fun getVisibleLines(totalLines: Int): List<LineIndicator> {
        val visibleLines = mutableListOf<LineIndicator>()
        var curr = 0
        while (curr <= totalLines) {

            when {
                cachedFoldRegions.firstOrNull { it.startLine == curr && it.isFolded } != null -> visibleLines.add(LineIndicator(curr, true))
                cachedFoldRegions.firstOrNull { it.foldedRange.contains(curr) && it.isFolded } == null -> visibleLines.add(LineIndicator(curr, false))
            }

            curr++
        }
        //nativeLog("Return visible lines: ${visibleLines.joinToString()}")
        return visibleLines
    }
}