package emulator.kit.assembler.gas

import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.Rule.Component.*
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*

enum class GASDirType(val disabled: Boolean = false, val contentStartsDirectly: Boolean = false, override val isSection: Boolean = false, override val rule: Rule? = null) : DirTypeInterface {
    ABORT(disabled = true, rule = Rule.dirNameRule("abort")),
    ALIGN(rule = Rule {
        Seq(
            Specific(".align", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.EXPRESSION_INTEGER),
                    Repeatable(maxLength = 2) {
                        Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                    }
                )
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
                    Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) },
                    Repeatable {
                        Seq(Specific(","), Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) })
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    BALIGNL(rule = Rule {
        Seq(
            Specific(".balignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )

    }),
    BALIGNW(rule = Rule {
        Seq(
            Specific(".balignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )

    }),
    COMM(rule = Rule {
        Seq(
            Specific(".comm", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            Repeatable { Except(Dir("ENDEF")) },
            Dir("ENDEF")
        )
    }),
    DESC(rule = Rule {
        Seq(
            Specific(".desc", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
                    SpecNode(GASNodeType.EXPRESSION_INTEGER),
                    Repeatable {
                        Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                    }
                )
            }
        )
    }),
    IDENT(rule = Rule.dirNameRule("ident")),
    IF(rule = Rule {
        Seq(
            Specific(".if", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFDEF(rule = Rule {
        Seq(
            Specific(".ifdef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(Dir("elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFB(rule = Rule {
        Seq(
            Specific(".ifb", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFC(rule = Rule {
        Seq(
            Specific(".ifc", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFEQ(rule = Rule {
        Seq(
            Specific(".ifeq", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFEQS(rule = Rule {
        Seq(
            Specific(".ifeqs", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFGE(rule = Rule {
        Seq(
            Specific(".ifge", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFGT(rule = Rule {
        Seq(
            Specific(".ifgt", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFLE(rule = Rule {
        Seq(
            Specific(".ifle", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFLT(rule = Rule {
        Seq(
            Specific(".iflt", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNB(rule = Rule {
        Seq(
            Specific(".ifnb", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNC(rule = Rule {
        Seq(
            Specific(".ifnc", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNDEF(rule = Rule {
        Seq(
            Specific(".ifndef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNOTDEF(rule = Rule {
        Seq(
            Specific(".ifnotdef", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNE(rule = Rule {
        Seq(
            Specific(".ifne", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNES(rule = Rule {
        Seq(
            Specific(".ifnes", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(Dir("elseif"), SpecNode(GASNodeType.EXPRESSION_STRING), Specific(","), SpecNode(GASNodeType.EXPRESSION_STRING), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    INCBIN(rule = Rule {
        Seq(
            Specific(".incbin", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_STRING), Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER), Optional {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
                    Except(XOR(Dir("ENDR"), InSpecific(Token.Type.LINEBREAK))),
                )
            },
            Repeatable {
                Except(Dir("ENDR"))
            }, Dir("ENDR")
        )
    }),
    IRPC(rule = Rule {
        Seq(
            Specific(".irpc", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            Optional { Except(XOR(Dir("ENDR"), InSpecific(emulator.kit.assembler.lexer.Token.Type.LINEBREAK))) },
            Repeatable {
                Except(Dir("ENDR"))
            }, Dir("ENDR")
        )
    }),
    LCOMM(rule = Rule {
        Seq(
            Specific(".lcomm", ignoreCase = true),
            InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER)
        )
    }),
    LFLAGS(rule = Rule.dirNameRule("lflags")),
    LINE(rule = Rule {
        Seq(
            Specific(".line", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    MACRO(rule = Rule {
        Seq(
            Dir("macro"),
            InSpecific(Token.Type.SYMBOL),
            Optional {
                Seq(
                    SpecNode(GASNodeType.ARG),
                    Repeatable {
                        Seq(
                            Optional {
                                Specific(",")
                            },
                            SpecNode(GASNodeType.ARG)
                        )
                    }
                )
            }
        )
    }),
    MRI(rule = Rule {
        Seq(
            Specific(".mri", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
        )
    }),
    NOALTMACRO(rule = Rule.dirNameRule("noaltmacro")),
    NOLIST(rule = Rule.dirNameRule("nolist")),
    NOP(rule = Rule {
        Seq(
            Specific(".nop", ignoreCase = true),
            Optional {
                SpecNode(GASNodeType.EXPRESSION_INTEGER)
            }
        )
    }),
    NOPS(rule = Rule {
        Seq(
            Specific(".nops", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
            }
        )
    }),
    OCTA(rule = Rule {
        Seq(
            Specific(".octa", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(GASNodeType.EXPRESSION_INTEGER),
                    Repeatable {
                        Seq(
                            Specific(","),
                            SpecNode(GASNodeType.EXPRESSION_INTEGER)
                        )
                    }
                )
            }
        )
    }),
    OFFSET(rule = Rule {
        Seq(
            Specific(".offset", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
        )
    }),
    ORG(disabled = true, rule = Rule.dirNameRule("org")),
    P2ALIGN(rule = Rule {
        Seq(
            Specific(".p2align", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    P2ALIGNW(rule = Rule {
        Seq(
            Specific(".p2alignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    P2ALIGNL(rule = Rule {
        Seq(
            Specific(".p2alignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    REPT(rule = Rule {
        Seq(
            Specific(".rept", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            XOR(Dir("DATA"), Dir("text"), Dir("RODATA"), Dir("BSS"), Seq(
                InSpecific(Token.Type.SYMBOL),
                Optional {
                    Seq(
                        Specific(","),
                        SpecNode(GASNodeType.EXPRESSION_STRING)
                    )
                }
            ))
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    SINGLE(disabled = true, rule = Rule.dirNameRule("single")),
    SIZE(rule = Rule.dirNameRule("size")),
    SKIP(rule = Rule {
        Seq(
            Specific(".skip", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
            }
        )
    }),
    SLEB128(rule = Rule {
        Seq(
            Specific(".sleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    SPACE(rule = Rule {
        Seq(
            Specific(".space", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Optional {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_INTEGER)
                )
            }
        )
    }),
    TYPE(rule = Rule.dirNameRule("type")),
    ULEB128(rule = Rule {
        Seq(
            Specific(".uleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    VAL(rule = Rule {
        Seq(
            Specific(".val", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
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
                Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Repeatable {
                    Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_INTEGER))
                })
            }
        )
    }),
    ZERO(rule = Rule {
        Seq(
            Specific(".zero", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER)
        )
    }),
    _2BYTE(rule = Rule {
        Seq(
            Specific(".2byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_INTEGER)
                )
            }
        )
    }),
    _4BYTE(rule = Rule {
        Seq(
            Specific(".4byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_INTEGER)
                )
            }
        )
    }),
    _8BYTE(rule = Rule {
        Seq(
            Specific(".8byte", ignoreCase = true),
            SpecNode(GASNodeType.EXPRESSION_INTEGER),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_INTEGER)
                )
            }
        )
    });

    override fun getDetectionString(): String = if (!this.contentStartsDirectly) this.name.removePrefix("_") else ""

    override fun buildDirectiveContent(tokens: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode.Directive? {
        val result = this.rule?.matchStart(tokens, allDirs, definedAssembly, listOf()) ?: return null
        if (result.matches) {
            return GASNode.Directive(this, result.matchingTokens + result.ignoredSpaces, result.matchingNodes)
        }
        return null
    }

    override fun executeDirective(stmnt: GASNode.Statement.Dir, cont: GASParser.TempContainer) {
        when (stmnt.dir.type) {
            // Data
            ALIGN -> {
                val exprs = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>()

                if (exprs.isEmpty()) {
                    return
                }

                val alignment = exprs[0].evaluate(true)

                val byte = if (exprs.size > 1) {
                    val dec32 = exprs[1].evaluate(true).toUDec()
                    if (!dec32.check(Bit8()).valid) throw Parser.ParserError(exprs[1].getAllTokens().first(), "Numeric Expression exceeds 8 Bits!")
                    dec32.toBin().getUResized(Bit8()).toHex()
                } else Hex("0", Bit8())

                val max = if (exprs.size > 2) {
                    exprs[2].evaluate(true)
                } else null

                val lastOffset = cont.currSection.lastOffset

                val padding = (alignment - (lastOffset % alignment)).toDec().toIntOrNull() ?: throw Parser.ParserError(exprs[0].getAllTokens().first(), "Couldn't convert Numeric Expr to Int!")
                if (padding == alignment.toIntOrNull()) return

                val refToken = stmnt.dir.getAllTokens().first()
                val word = Hex(byte.getRawHexStr().repeat(4), Bit32())
                val short = Hex(byte.getRawHexStr().repeat(2), Bit16())
                var index = 0
                while (index < padding) {
                    if (index + 3 < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, word, GASParser.Data.DataType.WORD))
                        index += 4
                        continue
                    }
                    if (index + 1 < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, short, GASParser.Data.DataType.SHORT))
                        index += 2
                        continue
                    }
                    if (index < padding) {
                        cont.currSection.addContent(GASParser.Data(refToken, byte, GASParser.Data.DataType.BYTE))
                        index += 1
                        continue
                    }
                }
            }

            ASCII -> {
                val ref = stmnt.getAllTokens().first()
                val string = stmnt.dir.additionalNodes.filterIsInstance<GASNode.StringExpr>().map {
                    it.evaluate(false)
                }.joinToString("") { it }
                val chunks = string.chunked(4).map {
                    Variable.Tools.asciiToHex(it.reversed())
                }
                chunks.forEach { hexStr ->
                    when (hexStr.length) {
                        8 -> {
                            cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit32()), GASParser.Data.DataType.WORD))
                        }

                        6 -> {
                            cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr.substring(2), Bit16()), GASParser.Data.DataType.SHORT))
                            cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr.substring(0, 2), Bit8()), GASParser.Data.DataType.BYTE))
                        }

                        4 -> {
                            cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit16()), GASParser.Data.DataType.SHORT))
                        }

                        2 -> {
                            cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit8()), GASParser.Data.DataType.BYTE))
                        }
                    }
                }
            }

            ASCIZ -> {
                val ref = stmnt.getAllTokens().first()
                val strings = stmnt.dir.additionalNodes.filterIsInstance<GASNode.StringExpr>().map {
                    it.evaluate(false)
                }

                strings.forEach { string ->
                    val chunks = string.chunked(4).map {
                        Variable.Tools.asciiToHex(it.reversed())
                    }
                    chunks.forEach { hexStr ->
                        when (hexStr.length) {
                            8 -> {
                                cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit32()), GASParser.Data.DataType.WORD))
                            }

                            6 -> {
                                cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr.substring(2), Bit16()), GASParser.Data.DataType.SHORT))
                                cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr.substring(0, 2), Bit8()), GASParser.Data.DataType.BYTE))
                            }

                            4 -> {
                                cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit16()), GASParser.Data.DataType.SHORT))
                            }

                            2 -> {
                                cont.currSection.addContent(GASParser.Data(ref, Hex(hexStr, Bit8()), GASParser.Data.DataType.BYTE))
                            }
                        }
                    }
                    cont.currSection.addContent(GASParser.Data(ref, Hex("0", Bit8()), GASParser.Data.DataType.BYTE))
                }
            }

            BYTE -> {
                val ref = stmnt.dir.getAllTokens().first()
                val bytes = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().map {
                    val value = it.evaluate(false).toBin()
                    val truncated = value.getUResized(Bit8()).toHex()
                    if (value.checkSizeUnsigned(Bit8()) != null) {
                        it.getAllTokens().first().addSeverity(Severity.Type.WARNING, "value ${value.toHex()} truncated to $truncated")
                    }
                    truncated
                }
                bytes.forEach {
                    cont.currSection.addContent(GASParser.Data(ref, it, GASParser.Data.DataType.BYTE))
                }
            }

            SHORT -> {
                val ref = stmnt.dir.getAllTokens().first()
                val bytes = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().map {
                    val value = it.evaluate(false).toBin()
                    val truncated = value.getUResized(Bit16()).toHex()
                    if (value.checkSizeUnsigned(Bit16()) != null) {
                        it.getAllTokens().first().addSeverity(Severity.Type.WARNING, "value ${value.toHex()} truncated to $truncated")
                    }
                    truncated
                }
                bytes.forEach {
                    cont.currSection.addContent(GASParser.Data(ref, it, GASParser.Data.DataType.SHORT))
                }
            }

            WORD -> {
                val ref = stmnt.dir.getAllTokens().first()
                val bytes = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().map {
                    val value = it.evaluate(false).toBin()
                    val truncated = value.getUResized(Bit32()).toHex()
                    if (value.checkSizeUnsigned(Bit32()) != null) {
                        it.getAllTokens().first().addSeverity(Severity.Type.WARNING, "value ${value.toHex()} truncated to $truncated")
                    }
                    truncated
                }
                bytes.forEach {
                    cont.currSection.addContent(GASParser.Data(ref, it, GASParser.Data.DataType.WORD))
                }
            }

            ZERO -> {
                val length = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()?.evaluate(false)?.toIntOrNull() ?: return

                val refToken = stmnt.dir.getAllTokens().first()
                var index = 0
                while (index < length) {
                    if (index + 3 < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroWord, GASParser.Data.DataType.WORD))
                        index += 4
                        continue
                    }
                    if (index + 1 < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroShort, GASParser.Data.DataType.SHORT))
                        index += 2
                        continue
                    }
                    if (index < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroByte, GASParser.Data.DataType.BYTE))
                        index += 1
                        continue
                    }
                }
            }

            // Symbols
            EQU -> {
                val symbolToken = stmnt.dir.allTokens.first { it.type == Token.Type.SYMBOL }
                symbolToken.hl(CodeStyle.symbol)
                val newSymbol = when (val expr = stmnt.dir.additionalNodes.first() as? GASNode) {
                    is GASNode.NumericExpr -> {
                        GASParser.Symbol.IntegerExpr(symbolToken.content, expr)
                    }

                    is GASNode.StringExpr -> {
                        GASParser.Symbol.StringExpr(symbolToken.content, expr)
                    }

                    else -> {
                        GASParser.Symbol.Undefined(symbolToken.content)
                    }
                }
                cont.setOrReplaceSymbol(newSymbol)
            }

            SET_ALT -> {
                val symbol = stmnt.dir.allTokens.first()
                val newSymbol = when (val expr = stmnt.dir.additionalNodes.first() as? GASNode) {
                    is GASNode.NumericExpr -> {
                        GASParser.Symbol.IntegerExpr(symbol.content, expr)
                    }

                    is GASNode.StringExpr -> {
                        GASParser.Symbol.StringExpr(symbol.content, expr)
                    }

                    else -> {
                        GASParser.Symbol.Undefined(symbol.content)
                    }
                }
                cont.setOrReplaceSymbol(newSymbol)
                stmnt.dir.allTokens.forEach { it.hl(CodeStyle.symbol) }
            }

            SET -> {
                val symbol = stmnt.dir.allTokens.first { it.type == Token.Type.SYMBOL }
                val newSymbol = when (val expr = stmnt.dir.additionalNodes.first() as? GASNode) {
                    is GASNode.NumericExpr -> {
                        GASParser.Symbol.IntegerExpr(symbol.content, expr)
                    }

                    is GASNode.StringExpr -> {
                        GASParser.Symbol.StringExpr(symbol.content, expr)
                    }

                    else -> {
                        GASParser.Symbol.Undefined(symbol.content)
                    }
                }
                cont.setOrReplaceSymbol(newSymbol)
                symbol.hl(CodeStyle.symbol)
            }

            COMM -> {
                val symbol = stmnt.dir.allTokens.first { it.type == Token.Type.SYMBOL }
                val alreadyDefined = cont.symbols.firstOrNull { it.name == symbol.content }

                if (alreadyDefined == null) {
                    cont.setOrReplaceSymbol(GASParser.Symbol.Undefined(symbol.content))
                }
                symbol.hl(CodeStyle.symbol)

                val length = when (alreadyDefined) {
                    is GASParser.Symbol.StringExpr -> alreadyDefined.expr.evaluate(false).length
                    is GASParser.Symbol.IntegerExpr -> alreadyDefined.expr.evaluate(false).size.getByteCount()
                    is GASParser.Symbol.TokenRef -> stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()?.evaluate(false)?.toIntOrNull() ?: return
                    is GASParser.Symbol.Undefined -> stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()?.evaluate(false)?.toIntOrNull() ?: return
                    null -> stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull()?.evaluate(false)?.toIntOrNull() ?: return
                }

                val refToken = stmnt.dir.getAllTokens().first()
                var index = 0
                while (index < length) {
                    if (index + 3 < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroWord, GASParser.Data.DataType.WORD))
                        index += 4
                        continue
                    }
                    if (index + 1 < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroShort, GASParser.Data.DataType.SHORT))
                        index += 2
                        continue
                    }
                    if (index < length) {
                        cont.currSection.addContent(GASParser.Data(refToken, zeroByte, GASParser.Data.DataType.BYTE))
                        index += 1
                        continue
                    }
                }
            }

            DESC -> {
                val symbol = stmnt.dir.allTokens.firstOrNull { it.type == Token.Type.SYMBOL } ?: return
                val expr = stmnt.dir.additionalNodes.filterIsInstance<GASNode.NumericExpr>().firstOrNull() ?: return
                val desc = expr.evaluate(true)
                if (desc.toBin().checkSizeUnsigned(Bit16()) != null) throw Parser.ParserError(expr.getAllTokens().first(), "Expression exceeds unsigned 16 Bits!")
                cont.setOrReplaceDescriptor(symbol.content, desc.toBin().getUResized(Bit16()).toHex())
            }

            // Sections
            DATA -> {
                cont.switchToOrAppendSec("data")
            }

            TEXT -> {
                cont.switchToOrAppendSec("text")
            }

            BSS -> {
                cont.switchToOrAppendSec("bss")
            }

            RODATA -> {
                cont.switchToOrAppendSec("rodata")
            }

            SECTION -> {
                val dirs = stmnt.dir.allTokens.filter { it.type == Token.Type.DIRECTIVE }

                if (dirs.size == 2) {
                    cont.switchToOrAppendSec(dirs[1].getContentAsString())
                    return
                }

                val symbol = stmnt.dir.allTokens.firstOrNull { it.type == Token.Type.SYMBOL }
                if (symbol == null) {
                    throw Parser.ParserError(stmnt.dir.allTokens.first(), "Section Directive expecting a section directive or at least one symbol!")
                }

                val flags = stmnt.dir.additionalNodes.filterIsInstance<GASNode.StringExpr>().firstOrNull()?.evaluate(false) ?: ""
                cont.switchToOrAppendSec(symbol.content, flags)
            }
        }
    }

    companion object {
        val zeroByte = Hex("0", Bit8())
        val zeroShort = Hex("0", Bit16())
        val zeroWord = Hex("0", Bit32())
    }
}