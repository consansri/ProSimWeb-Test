package emulator.kit.assembler.gas

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.Rule.Component.*
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

enum class GASDirType(val disabled: Boolean = false, val contentStartsDirectly: Boolean = false, override val isSection: Boolean = false, override val rule: Rule) : DirTypeInterface {
    ABORT(disabled = true, rule = Rule.dirNameRule("abort")),
    ALIGN(rule = Rule {
        Seq(
            Specific(".align", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    ALTMACRO(rule = Rule.dirNameRule("altmacro")),
    ASCII(rule = Rule {
        Seq(
            Specific(".ascii", ignoreCase = true),
            Optional {
                Seq(
                    Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) },
                    Repeatable {
                        Seq(Specific(","), Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) })
                    }
                )
            }
        )
    }),
    ASCIZ(rule = Rule {
        Seq(
            Specific(".asciz", ignoreCase = true),
            Optional {
                Seq(
                    Repeatable { SpecNode(emulator.kit.assembler.gas.nodes.GASNodeType.EXPRESSION_STRING) },
                    Repeatable {
                        Seq(Specific(","), Repeatable { SpecNode(emulator.kit.assembler.gas.nodes.GASNodeType.EXPRESSION_STRING) })
                    }
                )
            }
        )
    }),
    ATTACH_TO_GROUP_NAME(rule = Rule.dirNameRule("attach_to_group_name")),
    BALIGN(rule = Rule {
        Seq(
            Specific(".balign", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    BALIGNL(rule = Rule {
        Seq(
            Specific(".balignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )

    }),
    BALIGNW(rule = Rule {
        Seq(
            Specific(".balignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )

    }),
    BSS(isSection = true, rule = Rule {
        Seq(
            Specific(".bss", ignoreCase = true),
            Optional { InSpecific(Token.Type.SYMBOL) }
        )
    }),
    BYTE(rule = Rule {
        Seq(
            Specific(".byte", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )

    }),
    COMM(rule = Rule {
        Seq(
            Specific(".comm", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    DATA(isSection = true, rule = Rule {
        Seq(
            Specific(".data", ignoreCase = true),
            Optional { InSpecific(Token.Type.SYMBOL) }
        )
    }),
    DEF(rule = Rule {
        Seq(
            Specific(".def", ignoreCase = true),
            Repeatable { Except(Dir(".ENDEF")) },
            Dir(".ENDEF")
        )
    }),
    DESC(rule = Rule {
        Seq(
            Specific(".desc", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    DIM(rule = Rule.dirNameRule("dim")),
    DOUBLE(disabled = true, rule = Rule.dirNameRule("double")),
    EJECT(rule = Rule.dirNameRule("eject")),
    ELSE(rule = Rule.dirNameRule("else")),
    ELSEIF(rule = Rule.dirNameRule("elseif")),
    END(rule = Rule.dirNameRule("end")),
    ENDM(rule = Rule.dirNameRule("endm")),
    ENDR(rule = Rule.dirNameRule("endr")),
    ENDEF(rule = Rule.dirNameRule("endef")),
    ENDFUNC(rule = Rule.dirNameRule("endfunc")),
    ENDIF(rule = Rule.dirNameRule("endif")),
    EQU(rule = Rule {
        Seq(
            Specific(".equ", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    EQUIV(rule = Rule {
        Seq(
            Specific(".equiv", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    EQV(rule = Rule {
        Seq(
            Specific(".eqv", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    ERR(rule = Rule.dirNameRule("err")),
    ERROR(rule = Rule {
        Seq(
            Specific(".error", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    EXITM(rule = Rule.dirNameRule("exitm")),
    EXTERN(rule = Rule.dirNameRule("extern")),
    FAIL(rule = Rule {
        Seq(
            Specific(".fail", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    FILE(rule = Rule {
        Seq(
            Specific(".file", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    FILL(rule = Rule {
        Seq(
            Specific(".fill", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    FLOAT(disabled = true, rule = Rule.dirNameRule("float")),
    FUNC(rule = Rule {
        Seq(
            Specific(".func", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Optional { Seq(Specific(","), InSpecific(Token.Type.SYMBOL)) }
        )
    }),
    GLOBAL(rule = Rule {
        Seq(
            Specific(".global", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    GLOBL(rule = Rule {
        Seq(
            Specific(".globl", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    GNU_ATTRIBUTE(disabled = true, rule = Rule.dirNameRule("gnu_attribute")),
    HIDDEN(rule = Rule {
        Seq(
            Specific(".hidden", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Repeatable { Seq(Specific(","), InSpecific(Token.Type.SYMBOL)) }
        )
    }),
    HWORD(rule = Rule {
        Seq(
            Specific(".hword", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.EXPRESSION_ABS),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                    }
                )
            }
        )
    }),
    IDENT(rule = Rule.dirNameRule("ident")),
    IF(rule = Rule {
        Seq(
            Specific(".if", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFDEF(rule = Rule {
        Seq(
            Specific(".ifdef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            },
            Repeatable {
                Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Dir(".endif")
        )
    }),
    IFB(rule = Rule {
        Seq(
            Specific(".ifb", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            },
            Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Dir(".endif")
        )
    }),
    IFC(rule = Rule {
        Seq(
            Specific(".ifc", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            },
            Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Dir(".endif")
        )
    }),
    IFEQ(rule = Rule {
        Seq(
            Specific(".ifeq", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            },
            Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            },
            Dir(".endif")
        )
    }),
    IFEQS(rule = Rule {
        Seq(
            Specific(".ifeqs", ignoreCase = true),
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
        Seq(
            Specific(".ifge", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFGT(rule = Rule {
        Seq(
            Specific(".ifgt", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFLE(rule = Rule {
        Seq(
            Specific(".ifle", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFLT(rule = Rule {
        Seq(
            Specific(".iflt", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNB(rule = Rule {
        Seq(
            Specific(".ifnb", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNC(rule = Rule {
        Seq(
            Specific(".ifnc", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNDEF(rule = Rule {
        Seq(
            Specific(".ifndef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNOTDEF(rule = Rule {
        Seq(
            Specific(".ifnotdef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNE(rule = Rule {
        Seq(
            Specific(".ifne", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    IFNES(rule = Rule {
        Seq(
            Specific(".ifnes", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
            }, Repeatable {
                Seq(Dir(".elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Optional {
                Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
            }, Dir(".endif")
        )
    }),
    INCBIN(rule = Rule {
        Seq(
            Specific(".incbin", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS), Optional {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    INCLUDE(rule = Rule {
        Seq(
            Specific(".include", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    INT(rule = Rule {
        Seq(
            Specific(".int", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    INTERNAL(rule = Rule {
        Seq(
            Specific(".internal", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            }
        )
    }),
    IRP(rule = Rule {
        Seq(
            Specific(".irp", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Repeatable {
                Seq(
                    Specific(","),
                    Except(XOR(Dir(".ENDR"), InSpecific(Token.Type.LINEBREAK))),
                )
            },
            Repeatable {
                Except(Dir(".ENDR"))
            }, Dir(".ENDR")
        )
    }),
    IRPC(rule = Rule {
        Seq(
            Specific(".irpc", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            Optional { Except(XOR(Dir(".ENDR"), InSpecific(emulator.kit.assembler.lexer.Token.Type.LINEBREAK))) },
            Repeatable {
                Except(Dir(".ENDR"))
            }, Dir(".ENDR")
        )
    }),
    LCOMM(rule = Rule {
        Seq(
            Specific(".lcomm", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    LFLAGS(rule = Rule.dirNameRule("lflags")),
    LINE(rule = Rule {
        Seq(
            Specific(".line", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    LINKONCE(rule = Rule {
        Seq(
            Specific(".linkonce", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    LIST(rule = Rule.dirNameRule("list")),
    LN(rule = Rule {
        Seq(
            Specific(".ln", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    LOC(disabled = true, rule = Rule.dirNameRule("loc")),
    LOC_MARK_LABELS(disabled = true, rule = Rule.dirNameRule("loc_mark_labels")),
    LOCAL(rule = Rule {
        Seq(
            Specific(".local", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            }
        )
    }),
    LONG(rule = Rule {
        Seq(
            Specific(".long", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    MACRO(rule = Rule(ignoreSpace = false) {
        Seq(
            Specific(".macro", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
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
            InSpecific(emulator.kit.assembler.lexer.Token.Type.LINEBREAK),
            Repeatable {
                SpecNode(GASNodeType.STATEMENT)
            },
            Dir(".ENDM")
        )
    }),
    MRI(rule = Rule {
        Seq(
            Specific(".mri", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    NOALTMACRO(rule = Rule.dirNameRule("noaltmacro")),
    NOLIST(rule = Rule.dirNameRule("nolist")),
    NOP(rule = Rule {
        Seq(
            Specific(".nop", ignoreCase = true),
            Optional {
                SpecNode(GASNodeType.EXPRESSION_ABS)
            }
        )
    }),
    NOPS(rule = Rule {
        Seq(
            Specific(".nops", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    OCTA(rule = Rule {
        Seq(
            Specific(".octa", ignoreCase = true),
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
        )
    }),
    OFFSET(rule = Rule {
        Seq(
            Specific(".offset", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    ORG(disabled = true, rule = Rule.dirNameRule("org")),
    P2ALIGN(rule = Rule {
        Seq(
            Specific(".p2align", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    P2ALIGNW(rule = Rule {
        Seq(
            Specific(".p2alignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    P2ALIGNL(rule = Rule {
        Seq(
            Specific(".p2alignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    POPSECTION(rule = Rule.dirNameRule("popsection")),
    PREVIOUS(rule = Rule.dirNameRule("previous")),
    PRINT(rule = Rule {
        Seq(
            Specific(".print", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    PROTECTED(rule = Rule {
        Seq(
            Specific(".protected", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            }
        )
    }),
    PSIZE(rule = Rule {
        Seq(
            Specific(".psize", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    PURGEM(rule = Rule {
        Seq(
            Specific(".purgem", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    PUSHSECTION(rule = Rule {
        Seq(
            Specific(".pushsection", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Optional {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            }
        )
    }),
    QUAD(rule = Rule {
        Seq(
            Specific(".quad", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    RODATA(isSection = true, rule = Rule {
        Seq(
            Specific(".rodata", ignoreCase = true),
            Optional {
                InSpecific(emulator.kit.assembler.lexer.Token.Type.SYMBOL)
            }
        )
    }),
    RELOC(rule = Rule {
        Seq(
            Specific(".reloc", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    REPT(rule = Rule {
        Seq(
            Specific(".rept", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    SBTTL(rule = Rule {
        Seq(
            Specific(".sbttl", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    SCL(rule = Rule {
        Seq(
            Specific(".scl", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    SECTION(isSection = true, rule = Rule {
        Seq(
            Specific(".section", ignoreCase = true),
            XOR(Dir(".DATA"), Dir(".TEXT"), Dir(".RODATA"), Dir(".BSS"))
        )
    }),
    SET_ALT(contentStartsDirectly = true, rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
            Specific("="),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    SET(rule = Rule {
        Seq(
            Specific(".set", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ANY)
        )
    }),
    SHORT(rule = Rule {
        Seq(
            Specific(".short", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    SINGLE(disabled = true, rule = Rule.dirNameRule("single")),
    SIZE(rule = Rule.dirNameRule("size")),
    SKIP(rule = Rule {
        Seq(
            Specific(".skip", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    SLEB128(rule = Rule {
        Seq(
            Specific(".sleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    SPACE(rule = Rule {
        Seq(
            Specific(".space", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    STABD(rule = Rule.dirNameRule("stabd")),
    STABN(rule = Rule.dirNameRule("stabn")),
    STABS(rule = Rule.dirNameRule("stabs")),
    STRING(rule = Rule {
        Seq(
            Specific(".string", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING8(rule = Rule {
        Seq(
            Specific(".string8", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING16(rule = Rule {
        Seq(
            Specific(".string16", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRUCT(rule = Rule {
        Seq(
            Specific(".struct", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    SUBSECTION(rule = Rule {
        Seq(
            Specific(".subsection", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    SYMVER(rule = Rule.dirNameRule("symver")),
    TAG(rule = Rule {
        Seq(
            Specific(".tag", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    TEXT(rule = Rule {
        Seq(
            Specific(".text", ignoreCase = true),
            Optional {
                InSpecific(Token.Type.SYMBOL)
            }
        )
    }),
    TITLE(rule = Rule {
        Seq(
            Specific(".title", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    TLS_COMMON(rule = Rule {
        Seq(
            Specific(".tls_common", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Optional {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    TYPE(rule = Rule.dirNameRule("type")),
    ULEB128(rule = Rule {
        Seq(
            emulator.kit.assembler.Rule.Component.Specific(".uleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    VAL(rule = Rule {
        Seq(
            Specific(".val", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    VERSION(rule = Rule {
        Seq(
            Specific(".version", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )
    }),
    VTABLE_ENTRY(rule = Rule {
        Seq(
            Specific(".vtable_entry", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    VTABLE_INHERIT(rule = Rule {
        Seq(
            Specific(".vtable_inherit", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    WARNING(rule = Rule {
        Seq(
            Specific(".warning", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING)
        )

    }),
    WEAK(rule = Rule {
        Seq(
            Specific(".weak", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Repeatable {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            })
    }),
    WEAKREF(rule = Rule {
        Seq(
            Specific(".weakref", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    WORD(rule = Rule {
        Seq(
            Specific(".word", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
                })
            }
        )
    }),
    ZERO(rule = Rule {
        Seq(
            Specific(".zero", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    _2BYTE(rule = Rule {
        Seq(
            Specific(".2byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    _4BYTE(rule = Rule {
        Seq(
            Specific(".4byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    }),
    _8BYTE(rule = Rule {
        Seq(
            Specific(".8byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    });

    override fun getDetectionString(): String = if (!this.contentStartsDirectly) this.name.removePrefix("_") else ""

    override fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive? {
        val result = this.rule.matchStart(tokens, allDirs, definedAssembly)
        if (result.matches) {
            return GASNode.Directive(this, result.matchingTokens, result.matchingNodes)
        }
        return null
    }

}