package cengine.editor.highlighting

import cengine.psi.core.PsiFile

interface HighlightProvider {

    val cachedHighlights: MutableMap<PsiFile, List<HLInfo>>

    fun updateHighlights(psiFile: PsiFile)

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String): List<HLInfo>
}