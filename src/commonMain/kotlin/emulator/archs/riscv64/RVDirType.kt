package emulator.archs.riscv64

import emulator.kit.assembler.DefinedAssembly
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.syntax.Rule
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.gas.GASDirType
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Tools.toValue
import kotlin.math.pow
import kotlin.math.roundToInt

enum class RVDirType(override val isSection: Boolean = false, override val rule: Rule? = null) : DirTypeInterface {
    ATTRIBUTE(rule = Rule {
        Seq(
            Specific(".attribute", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Except(Specific(",")),
            Specific(","),
            SpecNode(GASNodeType.ANY_EXPR)
        )
    }),
    ALIGN(rule = Rule {
        Seq(
            Specific(".align", ignoreCase = true),
            SpecNode(GASNodeType.INT_EXPR),
        )
    }),
    DTPRELWORD(rule = Rule {
        Seq(
            Specific(".dtprelword", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DTPRELDWORD(rule = Rule {
        Seq(
            Specific(".dtpreldword", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DWORD(rule = Rule {
        Seq(
            Specific(".dword", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    HALF(rule = Rule {
        Seq(
            Specific(".half", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    OPTION(
        rule = Rule {
            Seq(
                Specific(".option", ignoreCase = true),
                InSpecific(Token.Type.SYMBOL)
            )
        }
    ),


    ;

    override fun getDetectionString(): String = this.name

    override fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive? {
        val result = this.rule?.matchStart(tokens, allDirs, definedAssembly, listOf()) ?: return null
        if (result.matches) {
            return GASNode.Directive(this, result.matchingTokens + result.ignoredSpaces, result.matchingNodes)
        }
        return null
    }

    override fun executeDirective(stmnt: GASNode.Statement.Dir, cont: GASParser.TempContainer) {
        when (stmnt.dir.type) {
            ALIGN -> {
                val exprs = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>()

                if (exprs.isEmpty()) {
                    return
                }

                val alignment = 2.0.pow(exprs[0].evaluate(true).toIntOrNull() ?: 0).roundToInt().toValue(cont.definedAssembly.memAddrSize)

                val lastOffset = cont.currSection.getLastAddress()

                val padding = (alignment - (lastOffset % alignment)).toDec().toIntOrNull() ?: throw Parser.ParserError(exprs[0].tokens().first(), "Couldn't convert Numeric Expr to Int!")
                if (padding == alignment.toIntOrNull()) return

                val refToken = stmnt.dir.tokens().first()

                var index = 0
                while (index < padding) {
                    if (index + 3 < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, GASDirType.zeroWord, GASParser.Data.DataType.WORD))
                        index += 4
                        continue
                    }
                    if (index + 1 < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, GASDirType.zeroShort, GASParser.Data.DataType.SHORT))
                        index += 2
                        continue
                    }
                    if (index < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, GASDirType.zeroByte, GASParser.Data.DataType.BYTE))
                        index += 1
                        continue
                    }
                }
            }

            HALF -> {
                GASDirType.HWORD.executeDirective(stmnt, cont)
            }

            DWORD -> {
                val ref = stmnt.dir.tokens().first()
                val shorts = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().map {
                    val value = it.evaluate(false).toBin()
                    val truncated = value.getUResized(Variable.Size.Bit64()).toHex()
                    if (value.checkSizeUnsigned(Variable.Size.Bit64()) != null) {
                        it.tokens().first().addSeverity(Severity.Type.WARNING, "value ${value.toHex()} truncated to $truncated")
                    }
                    truncated
                }
                shorts.forEach {
                    cont.currSection.addContent(GASParser.Data(ref, it, GASParser.Data.DataType.DWORD))
                }
            }

            else -> {
                stmnt.tokens().firstOrNull()?.addSeverity(Severity.Type.WARNING, "Not yet Implemented! ($this)")
            }
        }
    }


}