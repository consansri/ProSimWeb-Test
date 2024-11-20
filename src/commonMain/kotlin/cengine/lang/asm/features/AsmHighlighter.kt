package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.TargetSpec
import cengine.psi.core.Interval
import cengine.psi.core.PsiElement

class AsmHighlighter(targetSpec: TargetSpec<*>) : HighlightProvider {
    private val cache = mutableMapOf<PsiElement, List<HLInfo>>()

    private val lexer = targetSpec.createLexer("")

    override fun fastHighlight(text: String, inRange: IntRange): List<HLInfo> {
        lexer.reset(text)
        lexer.position = inRange.first
        val highlights = mutableListOf<HLInfo>()
        while (lexer.position <= inRange.last) {
            if (!lexer.hasMoreTokens()) {
                break
            }

            val token = lexer.consume(ignoreLeadingSpaces = true, ignoreComments = false)

            val style = token.type.style

            style?.let {
                highlights.add(HL(token, it))
            }
        }

        return highlights
    }

    data class HL(val element: Interval, val style: CodeStyle) : HLInfo {
        override val range: IntRange
            get() = element.range
        override val color: Int get() = style.getDarkElseLight()
        override fun toString(): String {
            return "<$range:${style.name}>"
        }
    }
}