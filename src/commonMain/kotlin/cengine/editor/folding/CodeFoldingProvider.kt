package cengine.editor.folding

import cengine.psi.core.PsiFile

interface CodeFoldingProvider {
    var cachedFoldRegions: List<FoldRegion>
    fun getFoldingRegions(psiFile: PsiFile): List<FoldRegion>

    fun getVisibleLines(totalLines: Int): List<LineIndicator> {
        val visibleLines = mutableListOf<LineIndicator>()
        var curr = 0
        while (curr < totalLines) {

            val startLineFoldRegion = cachedFoldRegions.firstOrNull { it.startLine == curr && it.isFolded }
            val inFoldedRegion = cachedFoldRegions.firstOrNull { it.foldedRange.contains(curr) && it.isFolded }

            when {
                startLineFoldRegion != null -> visibleLines.add(LineIndicator(curr, true, startLineFoldRegion.placeholder))
                inFoldedRegion == null -> visibleLines.add(LineIndicator(curr, false, ""))
            }

            curr++
        }
        //nativeLog("Return visible lines: ${visibleLines.joinToString()}")
        return visibleLines
    }
}