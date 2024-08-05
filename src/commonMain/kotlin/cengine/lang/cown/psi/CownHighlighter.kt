package cengine.lang.cown.psi

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.Highlight
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.cown.CownLexer
import cengine.psi.core.Locatable
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

class CownHighlighter : HighlightProvider {
    override var cachedHighlights: MutableMap<PsiFile, List<HLInfo>> = mutableMapOf()
    override fun updateHighlights(psiFile: PsiFile) {

    }

    override fun fastHighlight(text: String): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        var index = 0
        while (index < text.length) {
            val comparable = text.substring(index)
            val keyWord = CownLexer.keywords.firstOrNull { comparable.startsWith(it) }
            if (keyWord != null) {
                highlights.add(Highlight(object : Locatable {
                    override val textRange: TextRange = TextRange(index, index + keyWord.length)
                }, Highlight.Type.KEYWORD))
                index += keyWord.length
            } else {
                index++
            }
        }

        return highlights
    }
}