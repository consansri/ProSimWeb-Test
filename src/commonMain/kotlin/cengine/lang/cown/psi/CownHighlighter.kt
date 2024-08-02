package cengine.lang.cown.psi

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.cown.CownLexer
import cengine.psi.core.PsiFile

class CownHighlighter : HighlightProvider {
    override var cachedHighlights: List<HLInfo> = listOf(Highlight(0..3, Highlight.Type.COMMENT))

    override fun getHighlights(psiFile: PsiFile): List<Highlight> {
        val hls = mutableListOf<Highlight>()
        cachedHighlights = hls
        return hls
    }

    override fun fastHighlight(text: String): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        var index = 0
        while (index < text.length) {
            val comparable = text.substring(index)
            val keyWord = CownLexer.keywords.firstOrNull { comparable.startsWith(it) }
            if (keyWord != null) {
                highlights.add(Highlight(index..<(index + keyWord.length), Highlight.Type.KEYWORD))
                index += keyWord.length
            } else {
                index++
            }
        }

        return highlights
    }
}