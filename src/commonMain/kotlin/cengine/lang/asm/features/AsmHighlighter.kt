package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.psi.core.Interval
import cengine.psi.core.PsiElement

class AsmHighlighter(asmSpec: AsmSpec) : HighlightProvider {
    private val cache = mutableMapOf<PsiElement, List<HLInfo>>()

    private val lexer = asmSpec.createLexer("")

    override fun getHighlights(element: PsiElement): List<HLInfo> {
        if (element !is ASNode) return emptyList()
        return cache.getOrPut(element) {
            when (element) {
                is ASNode.ArgDef.Named -> listOf(HL(element, CodeStyle.argument))
                is ASNode.Argument.Basic -> listOf(HL(element, CodeStyle.argument))
                is ASNode.Argument.DefaultValue -> listOf(HL(element, CodeStyle.argument))
                is ASNode.Label -> listOf(HL(element, CodeStyle.label))
                is ASNode.NumericExpr.Operand.Identifier -> {
                    val ref = element.referencedElement
                    if (ref != null && ref is ASNode.Label) {
                        listOf(HL(element, CodeStyle.label))
                    } else emptyList()
                }
                is ASNode.StringExpr.Operand.Identifier -> {
                    val ref = element.referencedElement
                    if (ref != null && ref is ASNode.Label) {
                        listOf(HL(element, CodeStyle.label))
                    } else emptyList()
                }
                is ASNode.NumericExpr.Operand.Char -> listOf(HL(element, CodeStyle.char))
                is ASNode.NumericExpr.Operand.Number -> listOf(HL(element, CodeStyle.integer))
                is ASNode.StringExpr.Operand.StringLiteral -> listOf(HL(element, CodeStyle.string))
                is ASNode.Directive -> {
                    when (element.type) {
                        ASDirType.MACRO -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        ASDirType.SET_ALT -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        ASDirType.SET -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        ASDirType.EQU -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        ASDirType.EQV -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            identifier?.let {
                                listOf(HL(identifier, CodeStyle.symbol))
                            } ?: emptyList()
                        }

                        else -> emptyList()
                    }
                }

                is ASNode.Comment -> listOf(HL(element, CodeStyle.comment))
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