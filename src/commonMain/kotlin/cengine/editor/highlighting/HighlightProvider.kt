package cengine.editor.highlighting

import cengine.psi.core.PsiElement

interface HighlightProvider {
    fun getHighlights(element: PsiElement): List<HLInfo>

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String): List<HLInfo>
}