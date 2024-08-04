package cengine.lang.cown.psi

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegion
import cengine.editor.text.Informational
import cengine.psi.core.PsiFile

class CownFolder: CodeFoldingProvider {
    override var cachedFoldRegions: MutableMap<PsiFile, List<FoldRegion>> = mutableMapOf()
    override fun updateFoldRegions(psiFile: PsiFile, informational: Informational) {

    }


}