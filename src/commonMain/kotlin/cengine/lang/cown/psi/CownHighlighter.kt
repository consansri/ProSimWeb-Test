package cengine.lang.cown.psi

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.cown.CownLexer
import cengine.psi.core.Interval
import cengine.psi.core.PsiElement


class CownHighlighter : HighlightProvider {
    override fun getHighlights(element: PsiElement): List<HLInfo> {
        return emptyList()
    }

    override fun fastHighlight(text: String): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        var index = 0
        while (index < text.length) {
            val comparable = text.substring(index)
            val keyWord = CownLexer.keywords.firstOrNull { comparable.startsWith(it) }
            if (keyWord != null) {
                highlights.add(Highlight(object : Interval {
                    override val range: IntRange = index..<index + keyWord.length
                }, Highlight.Type.KEYWORD))
                index += keyWord.length
            } else {
                index++
            }
        }

        return highlights
    }
}