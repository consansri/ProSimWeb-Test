package emulator.kit.compiler.gas

import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.Rule
import emulator.kit.compiler.Rule.Component.*
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.gas.nodes.GASNodeType
import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node

enum class GASDirType(val disabled: Boolean = false, override val isSection: Boolean = false, override val rule: Rule = GASDirType.EMPTY) : DirTypeInterface {
    ABORT(disabled = true),
    ALIGN(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
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
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    BALIGNL(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    BALIGNW(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    BSS(isSection = true, rule = Rule {
        Optional { InSpecific(Token.SYMBOL::class) }
    }),
    BYTE(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    COMM(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    DATA(isSection = true, rule = Rule {
        Optional { InSpecific(Token.SYMBOL::class) }
    }),
    DEF(rule = Rule {
        Seq(Repeatable {
            Except(Dir(".ENDEF"))
        }, Dir(".ENDEF"))
    }),
    DESC(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    DIM,
    DOUBLE(disabled = true),
    EJECT,
    ELSE,
    ELSEIF,
    END,
    ENDM,
    ENDR,
    ENDEF,
    ENDFUNC,
    ENDIF,
    EQU(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    EQUIV(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    EQV(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    ERR,
    ERROR(rule = Rule {
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    EXITM,
    EXTERN,
    FAIL(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ANY)
    }),
    FILE(rule = Rule {
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    FILL(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    FLOAT(disabled = true),
    FUNC(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Optional {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    GLOBAL(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    GLOBL(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    GNU_ATTRIBUTE(disabled = true),
    HIDDEN(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    HWORD(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    IDENT,
    IF(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFDEF(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.SYMBOL::class), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFB(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFC(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFEQ(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFEQS(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFGE(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFGT(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFLE(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFLT(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNB(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNC(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNDEF(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.SYMBOL::class), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNOTDEF(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.SYMBOL::class), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNE(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNES(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    INCBIN(rule = Rule {
        Seq(InSpecific(Token.LITERAL.CHARACTER.STRING::class), Optional {
            Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS), Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        })
    }),
    INCLUDE(rule = Rule {
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    INT(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    INTERNAL(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    IRP(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Repeatable {
                Seq(
                    Specific(","),
                    Except(XOR(Dir(".ENDR"), InSpecific(Token.LINEBREAK::class))),
                )
            },
            Repeatable {
                Except(Dir(".ENDR"))
            }, Dir(".ENDR")
        )
    }),
    IRPC(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            Optional { Except(XOR(Dir(".ENDR"), InSpecific(Token.LINEBREAK::class))) },
            Repeatable {
                Except(Dir(".ENDR"))
            }, Dir(".ENDR")
        )
    }),
    LCOMM(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    LFLAGS,
    LINE(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    LINKONCE(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    LIST,
    LN(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    LOC(disabled = true),
    LOC_MARK_LABELS(disabled = true),
    LOCAL(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    LONG(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    MACRO(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Optional {
                Seq(
                    SpecNode(GASNodeType.IDENTIFIER),
                    Repeatable {
                        Seq(
                            Specific(","),
                            SpecNode(GASNodeType.IDENTIFIER),
                        )
                    }
                )
            },
            InSpecific(Token.LINEBREAK::class),
            Repeatable {
                SpecNode(GASNodeType.STATEMENT)
            },
            Dir(".ENDM")
        )
    }),
    MRI(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    NOALTMACRO,
    NOLIST,
    NOP(rule = Rule {
        Optional {
            SpecNode(GASNodeType.EXPRESSION_ABS)
        }
    }),
    NOPS(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    OCTA(rule = Rule {
        Optional {
            Seq(
                SpecNode(GASNodeType.EXPRESSION_ABS),
                Repeatable {
                    Seq(
                        Specific(","),
                        SpecNode(GASNodeType.EXPRESSION_ABS)
                    )
                }
            )
        }
    }),
    OFFSET(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    ORG(disabled = true),
    P2ALIGN(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    P2ALIGNW(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    P2ALIGNL(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    POPSECTION,
    PREVIOUS,
    PRINT(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    PROTECTED(rule = Rule {
        Seq(InSpecific(Token.SYMBOL::class), Repeatable {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    PSIZE(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    PURGEM(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    PUSHSECTION(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Optional {
                Seq(Specific(","), InSpecific(Token.SYMBOL::class))
            }
        )
    }),
    QUAD(rule = Rule {
        Optional {
            Seq(emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.compiler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    RODATA(isSection = true, rule = Rule{
        Optional {
            emulator.kit.compiler.Rule.Component.InSpecific(emulator.kit.compiler.lexer.Token.SYMBOL::class)
        }
    }),
    RELOC(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            InSpecific(Token.SYMBOL::class)
        )
    }),
    REPT(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    SBTTL(rule = Rule {
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    SCL(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    SECTION(isSection = true, rule = Rule {
        XOR(Dir(".DATA"), Dir(".TEXT"), Dir(".RODATA"), Dir(".BSS"))
    }),
    SET(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    SHORT(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    SINGLE(disabled = true),
    SIZE,
    SKIP(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    SLEB128(rule = Rule {
        Optional {
            Seq(emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.compiler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    SPACE(rule = Rule {
        Seq(
            emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS),
            emulator.kit.compiler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    STABD,
    STABN,
    STABS,
    STRING(rule = Rule {
        Seq(
            emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.compiler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING8(rule = Rule {
        Seq(
            emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.compiler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING16(rule = Rule {
        Seq(
            emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.compiler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRUCT(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    SUBSECTION(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    SYMVER,
    TAG(rule = Rule {
        InSpecific(Token.SYMBOL::class)
    }),
    TEXT(rule = Rule {
        Optional{
            InSpecific(Token.SYMBOL::class)
        }
    }),
    TITLE(rule = Rule {
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    TLS_COMMON(rule = Rule {
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional{
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    TYPE,
    ULEB128(rule = Rule {
        Optional {
            Seq(emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.compiler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.compiler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    VAL(rule = Rule{
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    VERSION(rule = Rule{
        InSpecific(Token.LITERAL.CHARACTER.STRING::class)
    }),
    VTABLE_ENTRY(rule = Rule{
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    VTABLE_INHERIT(rule = Rule{
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            InSpecific(Token.SYMBOL::class)
        )
    }),
    WARNING(rule = Rule{
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    WEAK(rule = Rule{
        Seq(InSpecific(Token.SYMBOL::class), emulator.kit.compiler.Rule.Component.Repeatable {
            Seq(Specific(","), InSpecific(Token.SYMBOL::class))
        })
    }),
    WEAKREF(rule = Rule{
        Seq(
            InSpecific(Token.SYMBOL::class),
            Specific(","),
            InSpecific(Token.SYMBOL::class)
        )
    }),
    WORD(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    ZERO(rule = Rule{
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    _2BYTE(rule = Rule{
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable{
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    _4BYTE(rule = Rule{
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable{
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    _8BYTE(rule = Rule{
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable{
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    });

    override fun getDetectionString(): String = this.name.removePrefix("_")

    override fun buildDirectiveContent(dirName: Token.KEYWORD.Directive, tokens: List<Token>, definedAssembly: DefinedAssembly): Node? {
        val result = this.rule.matchStart(tokens, definedAssembly)
        if (result.matches) {
            return GASNode.Directive(dirName, result.matchingTokens, result.matchingNodes)
        }
        return null
    }

    companion object {
        val EMPTY = Rule()
    }

}