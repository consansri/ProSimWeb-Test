package cengine.lang.cown.psi

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegionImpl
import cengine.psi.core.PsiFile

class CownFolder: CodeFoldingProvider {
    override var cachedFoldRegions: List<FoldRegionImpl> = listOf(FoldRegionImpl(23,27, false, "...}"), FoldRegionImpl(5,7, false, "...}"))

    override fun getFoldingRegions(psiFile: PsiFile): List<FoldRegionImpl> {
        return listOf()
    }
}