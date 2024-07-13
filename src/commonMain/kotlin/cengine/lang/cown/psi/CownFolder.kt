package cengine.lang.cown.psi

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegion
import cengine.psi.core.PsiFile

class CownFolder: CodeFoldingProvider {
    override var cachedFoldRegions: List<FoldRegion> = listOf(FoldRegion(23,27, false, "...}"), FoldRegion(5,7, false, "...}"))

    override fun getFoldingRegions(psiFile: PsiFile): List<FoldRegion> {
        return listOf()
    }
}