package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange

class AsmHighlighter(asmSpec: AsmSpec) : HighlightProvider {
    override val cachedHighlights: MutableMap<PsiFile, List<HLInfo>> = mutableMapOf()

    private val lexer = asmSpec.createLexer("")

    data class HL(override var range: TextRange, val style: CodeStyle) : HLInfo {
        override val color: Int get() = style.getDarkElseLight()
    }

    override fun updateHighlights(psiFile: PsiFile) {
        val builder = HighlightCollector()
        psiFile.accept(builder)
        cachedHighlights.remove(psiFile)
        cachedHighlights[psiFile] = builder.highlights
    }

    override fun fastHighlight(text: String): List<HLInfo> {
        lexer.reset(text)
        val highlights = mutableListOf<HLInfo>()
        while (true) {
            if (!lexer.hasMoreTokens()) {
                break
            }

            val token = lexer.consume(true, false)

            val style = token.type.style

            style?.let {
                highlights.add(HL(token.textRange, it))
            }
        }

        return highlights
    }

    inner class HighlightCollector : PsiElementVisitor {
        val highlights = mutableListOf<HLInfo>()
        override fun visitFile(file: PsiFile) {
            // Nothing needs to be done here
        }

        override fun visitElement(element: PsiElement) {
            if (element !is GASNode) return
            when (element) {
                is GASNode.ArgDef.Named -> highlights.add(HL(element.name.textRange, CodeStyle.argument))
                is GASNode.Argument.Basic -> highlights.add(HL(element.argName.textRange, CodeStyle.argument))
                is GASNode.Argument.DefaultValue -> highlights.add(HL(element.argName.textRange, CodeStyle.argument))
                is GASNode.Label -> highlights.add(HL(element.textRange, CodeStyle.label))
                is GASNode.NumericExpr.Operand.Char -> highlights.add(HL(element.char.textRange, CodeStyle.char))
                is GASNode.NumericExpr.Operand.Number -> highlights.add(HL(element.number.textRange, CodeStyle.integer))
                is GASNode.StringExpr.Operand.StringLiteral -> highlights.add(HL(element.string.textRange, CodeStyle.string))
                else -> {}
            }
        }

    }

}