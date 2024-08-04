package cengine.editor.folding

import cengine.editor.text.Informational
import cengine.psi.core.PsiFile

interface CodeFoldingProvider {
    val cachedFoldRegions: MutableMap<PsiFile, List<FoldRegion>>

    fun updateFoldRegions(psiFile: PsiFile, informational: Informational)

    fun getVisibleLines(psiFile: PsiFile?, totalLines: Int, informational: Informational): List<LineIndicator> {
        if (psiFile == null) return List(totalLines) {
            LineIndicator(it)
        }

        val visibleLines = mutableListOf<LineIndicator>()
        var curr = 0
        while (curr < totalLines) {
            val startLineFoldRegion = cachedFoldRegions[psiFile]?.firstOrNull { it.startLine == curr && it.isFolded }
            val inFoldedRegion = cachedFoldRegions[psiFile]?.firstOrNull { it.foldRange.contains(curr) && it.isFolded }

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