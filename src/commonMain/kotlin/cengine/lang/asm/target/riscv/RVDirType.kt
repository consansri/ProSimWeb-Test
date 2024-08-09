package cengine.lang.asm.target.riscv

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.lexer.AsmLexer
import cengine.lang.asm.lexer.AsmTokenType
import cengine.lang.asm.ast.Component
import cengine.lang.asm.ast.Rule

enum class RVDirType(override val isSection: Boolean = false, override val rule: Rule? = null) : DirTypeInterface {
    ATTRIBUTE(rule = Rule {
        Component.Seq(
            Component.Specific(".attribute", ignoreCase = true),
            Component.InSpecific(AsmTokenType.SYMBOL),
            Component.Except(Component.Specific(",")),
            Component.Specific(","),
            Component.SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    ALIGN(rule = Rule {
        Component.Seq(
            Component.Specific(".align", ignoreCase = true),
            Component.SpecNode(ASNodeType.INT_EXPR),
        )
    }),
    DTPRELWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dtprelword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DTPRELDWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dtpreldword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    HALF(rule = Rule {
        Component.Seq(
            Component.Specific(".half", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    OPTION(
        rule = Rule {
            Component.Seq(
                Component.Specific(".option", ignoreCase = true),
                Component.InSpecific(AsmTokenType.SYMBOL)
            )
        }
    ),


    ;

    override fun getDetectionString(): String = this.name

    override val typeName: String
        get() = name

    override fun buildDirectiveContent(lexer: AsmLexer, asmSpec: AsmSpec): ASNode.Directive? {
        val initialPos = lexer.position
        val result = this.rule?.matchStart(lexer, asmSpec)

        if (result == null) {
            lexer.position = initialPos
            return null
        }

        if (result.matches) {
            //nativeLog("RuleResult: ${result} for $this")
            val identificationToken = result.matchingTokens.firstOrNull { it.type == AsmTokenType.DIRECTIVE }
            return if (identificationToken != null) {
                ASNode.Directive(this, identificationToken, result.matchingTokens - identificationToken, result.matchingNodes)
            } else {
                ASNode.Directive(this, identificationToken, result.matchingTokens, result.matchingNodes)
            }
        }

        lexer.position = initialPos
        return null
    }
}