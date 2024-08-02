package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import emulator.kit.nativeLog

class AsmHighlighter(asmSpec: AsmSpec) : HighlightProvider {
    override var cachedHighlights: List<HLInfo> = listOf()
        set(value) {
            field = value
            nativeLog("Highlights: " + value.joinToString(" ") { it.toString() })
        }

    private val lexer = asmSpec.createLexer("")

    data class HL(override val range: IntRange, val style: CodeStyle) : HLInfo {
        override val color: Int get() = style.getDarkElseLight()
    }

    override fun getHighlights(psiFile: PsiFile): List<HLInfo> {
        val builder = HighlightCollector()
        psiFile.accept(builder)
        cachedHighlights = builder.highlights
        return builder.highlights
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
                highlights.add(HL(token.textRange.toIntRange(), it))
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
                is GASNode.ArgDef.Named -> highlights.add(HL(element.name.textRange.toIntRange(), CodeStyle.argument))
                is GASNode.Argument.Basic -> highlights.add(HL(element.argName.textRange.toIntRange(), CodeStyle.argument))
                is GASNode.Argument.DefaultValue -> highlights.add(HL(element.argName.textRange.toIntRange(), CodeStyle.argument))
                is GASNode.Label -> highlights.add(HL(element.textRange.toIntRange(), CodeStyle.label))
                is GASNode.NumericExpr.Operand.Char -> highlights.add(HL(element.char.textRange.toIntRange(), CodeStyle.char))
                is GASNode.NumericExpr.Operand.Number -> highlights.add(HL(element.number.textRange.toIntRange(), CodeStyle.integer))
                is GASNode.StringExpr.Operand.StringLiteral -> highlights.add(HL(element.string.textRange.toIntRange(), CodeStyle.string))
                else -> {}
            }
        }

    }

}