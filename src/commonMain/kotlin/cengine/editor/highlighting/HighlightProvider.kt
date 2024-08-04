package cengine.editor.highlighting

import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

interface HighlightProvider {

    val cachedHighlights: MutableMap<PsiFile, List<HLInfo>>

    fun insert(file: PsiFile, index: Int, length: Int) {
        cachedHighlights[file]?.let { hls ->
            hls.forEach {
                when {
                    index < it.range.startOffset.index -> {}

                    index in it.range -> {
                        it.range = TextRange(it.range.startOffset, it.range.endOffset + length)
                    }

                    index > it.range.endOffset.index -> {
                        it.range += length
                    }
                }
            }
        }
    }

    fun remove(file: PsiFile, start: Int, end: Int) {
        val isRemoved = mutableListOf<HLInfo>()
        cachedHighlights[file]?.let { hls ->
            hls.forEach {
                when {
                    end < it.range.startOffset.index -> {}
                    start > it.range.endOffset.index -> {
                        it.range += end - start
                    }

                    else -> {
                        isRemoved.add(it)
                    }
                }
            }
            cachedHighlights.remove(file)
            cachedHighlights[file] = hls - isRemoved
        }
    }

    fun updateHighlights(psiFile: PsiFile)

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String): List<HLInfo>
}