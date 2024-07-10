package cengine.editor.highlighting

import cengine.psi.core.PsiFile

interface HighlightProvider {

    var cachedHighlights: List<Highlight>

    fun getHighlights(psiFile: PsiFile): List<Highlight>

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String): List<Highlight>
}