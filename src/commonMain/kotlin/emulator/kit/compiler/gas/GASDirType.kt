package emulator.kit.compiler.gas

import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.Rule
import emulator.kit.compiler.Rule.Component.*
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node

enum class GASDirType(val disabled: Boolean = false, override val isSection: Boolean = false, override val rule: Rule = GASDirType.EMPTY) : DirTypeInterface {
    ABORT(disabled = true),
    ALIGN(rule = Rule {
        Optional {
            Seq(Expression, Repeatable(maxLength = 2) {
                Seq(Specific(","), Expression)
            })
        }
    }),
    ALTMACRO,
    ASCII(rule = Rule {
        Optional {
            Seq(
                Repeatable { InSpecific(Token.LITERAL.CHARACTER.STRING::class) },
                Repeatable {
                    Seq(Specific(","), Repeatable { InSpecific(Token.LITERAL.CHARACTER.STRING::class) })
                }
            )
        }
    }),
    ASCIZ(rule = Rule {
        Optional {
            Seq(
                Repeatable { InSpecific(Token.LITERAL.CHARACTER.STRING::class) },
                Repeatable {
                    Seq(Specific(","), Repeatable { InSpecific(Token.LITERAL.CHARACTER.STRING::class) })
                }
            )
        }
    }),
    ATTACH_TO_GROUP_NAME,
    BALIGN(rule = Rule {
        Optional {
            Seq(Expression, Repeatable(maxLength = 2) {
                Seq(Specific(","), Expression)
            })
        }
    }),
    BALIGN1(rule = Rule {
        Optional {
            Seq(Expression, Repeatable(maxLength = 2) {
                Seq(Specific(","), Expression)
            })
        }
    }),
    BALIGNW(rule = Rule {
        Optional {
            Seq(Expression, Repeatable(maxLength = 2) {
                Seq(Specific(","), Expression)
            })
        }
    }),
    BSS(isSection = true, rule = Rule {
        Optional { InSpecific(Token.SYMBOL::class) }
    }),
    BYTE(rule = Rule {
        Optional {
            Seq(Expression, Repeatable {
                Seq(Specific(","), Expression)
            })
        }
    }),
    COMM(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), Expression)
    }),
    DATA(isSection = true, rule = Rule {
        Optional { InSpecific(Token.SYMBOL::class) }
    }),
    DEF(),
    DESC,
    DIM,
    DOUBLE,
    EJECT,
    ELSE,
    ELSEIF,
    END,
    ENDEF,
    ENDFUNC,
    ENDIF,
    EQU,
    EQUIV,
    EQV,
    ERR,
    ERROR,
    EXITM,
    EXTERN,
    FAIL,
    FILE,
    FILL,
    FLOAT,
    FUNC,
    GLOBAL,
    GNU_ATTRIBUTE_TAG,
    HIDDEN,
    HWORD,
    IDENT,
    IF,
    INCBIN,
    INCLUDE,
    INT,
    INTERNAL,
    IRP,
    IRPC,
    LCOMM,
    LFLAGS,
    LINE,
    LINKONCE,
    LIST,
    LN,
    LOC,
    LOC_MARK_LABELS,
    LOCAL,
    LONG,
    MACRO,
    MRI,
    NOALTMACRO,
    NOLIST,
    NOP,
    NOPS,
    OCTA,
    OFFSET,
    ORG,
    P2ALIGN,
    POPSECTION,
    PREVIOUS,
    PRINT,
    PROTECTED,
    PSIZE,
    PURGEM,
    PUSHSECTION,
    QUAD,
    RELOC,
    REPT,
    SBTTL,
    SCL,
    SECTION(isSection = true),
    SET,
    SHORT,
    SINGLE,
    SIZE,
    SKIP,
    SLEB128,
    SPACE,
    STABD,
    STABN,
    STABS,
    STRING,
    STRING8,
    STRING16,
    STRUCT,
    SUBSECTION,
    SYMVER,
    TAG,
    TEXT,
    TITLE,
    TLS_COMMON_SYMBOL,
    TYPE,
    ULEB128,
    VAL,
    VERSION,
    VTABLE_ENTRY,
    VTABLE_INHERIT,
    WARNING,
    WEAK,
    WEAKREF,
    WORD,
    ZERO,
    _2BYTE,
    _4BYTE,
    _8BYTE;

    override fun getDetectionString(): String = this.name.removePrefix("_")

    override fun buildDirectiveContent(dirName: Token.KEYWORD.Directive, tokens: List<Token>): Node? {
        val result = this.rule.matchStart(tokens)
        if (result.matches) {
            return GASNode.Directive(dirName, result.matchingTokens, result.matchingNodes)
        }
        return null
    }

    companion object {
        val EMPTY = Rule()
    }

}