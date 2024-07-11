package cengine.lang.cown.psi

import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.core.PsiFile

class CownHighlighter: HighlightProvider {
    override var cachedHighlights: List<Highlight> = listOf(Highlight(0..3, Highlight.Type.COMMENT))

    override fun getHighlights(psiFile: PsiFile): List<Highlight> {
        val hls = mutableListOf<Highlight>()
        cachedHighlights = hls
        return hls
    }

    override fun fastHighlight(text: String): List<Highlight> {
        return listOf(Highlight(4..<text.length, Highlight.Type.KEYWORD))
    }
}