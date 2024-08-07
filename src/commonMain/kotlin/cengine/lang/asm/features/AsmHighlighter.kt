package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASDirType
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmTokenType
import cengine.psi.core.Interval
import cengine.psi.core.PsiElement

class AsmHighlighter(asmSpec: AsmSpec) : HighlightProvider {
    private val cache = mutableMapOf<PsiElement, List<HLInfo>>()

    private val lexer = asmSpec.createLexer("")

    override fun getHighlights(element: PsiElement): List<HLInfo> {
        if (element !is GASNode) return emptyList()
        return cache.getOrPut(element){
            when (element) {
                is GASNode.ArgDef.Named -> listOf(HL(element, CodeStyle.argument))
                is GASNode.Argument.Basic -> listOf(HL(element, CodeStyle.argument))
                is GASNode.Argument.DefaultValue -> listOf(HL(element, CodeStyle.argument))
                is GASNode.Label -> listOf(HL(element, CodeStyle.label))
                is GASNode.NumericExpr.Operand.Char -> listOf(HL(element, CodeStyle.char))
                is GASNode.NumericExpr.Operand.Number -> listOf(HL(element, CodeStyle.integer))
                is GASNode.StringExpr.Operand.StringLiteral -> listOf(HL(element, CodeStyle.string))
                is GASNode.Directive -> {
                    when (element.type) {
                        GASDirType.MACRO -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        GASDirType.SET_ALT -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        GASDirType.SET -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        GASDirType.EQU -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        GASDirType.EQV -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        else -> emptyList()
                    }
                }

                is GASNode.Comment -> listOf(HL(element, CodeStyle.comment))
                else -> {
                    emptyList()
                }
            }
        }
    }

    override fun fastHighlight(text: String): List<HLInfo> {
        lexer.reset(text)
        val highlights = mutableListOf<HLInfo>()
        while (true) {
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