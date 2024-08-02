package cengine.editor.folding

import cengine.editor.text.Informational
import cengine.psi.core.PsiFile

interface CodeFoldingProvider {
    var cachedFoldRegions: List<FoldRegionImpl>
    fun getFoldingRegions(psiFile: PsiFile, informational: Informational): List<FoldRegionImpl>

    fun getVisibleLines(totalLines: Int, informational: Informational): List<LineIndicator> {
        val visibleLines = mutableListOf<LineIndicator>()
        var curr = 0
        while (curr < totalLines) {
            val startLineFoldRegion = cachedFoldRegions.firstOrNull { it.startLine == curr && it.isFolded }
            val inFoldedRegion = cachedFoldRegions.firstOrNull {  it.foldRange.contains(curr) && it.isFolded }

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