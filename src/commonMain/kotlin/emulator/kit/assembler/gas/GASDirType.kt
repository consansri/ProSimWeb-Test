package emulator.kit.assembler.gas

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.Rule.Component.*
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

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
                Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) },
                Repeatable {
                    Seq(Specific(","), Repeatable { SpecNode(GASNodeType.EXPRESSION_STRING) })
                }
            )
        }
    }),
    ASCIZ(rule = Rule {
        Optional {
            Seq(
                Repeatable { emulator.kit.assembler.Rule.Component.SpecNode(emulator.kit.assembler.gas.nodes.GASNodeType.EXPRESSION_STRING) },
                Repeatable {
                    Seq(Specific(","), Repeatable { emulator.kit.assembler.Rule.Component.SpecNode(emulator.kit.assembler.gas.nodes.GASNodeType.EXPRESSION_STRING) })
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
        Optional { InSpecific(Token.Type.SYMBOL) }
    }),
    BYTE(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    COMM(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    DATA(isSection = true, rule = Rule {
        Optional { InSpecific(Token.Type.SYMBOL) }
    }),
    DEF(rule = Rule {
        Seq(Repeatable {
            Except(Dir(".ENDEF"))
        }, Dir(".ENDEF"))
    }),
    DESC(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
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
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    EQUIV(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    EQV(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ANY))
    }),
    ERR,
    ERROR(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    EXITM,
    EXTERN,
    FAIL(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ANY)
    }),
    FILE(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    FILL(rule = Rule {
        Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    FLOAT(disabled = true),
    FUNC(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Optional {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
        })
    }),
    GLOBAL(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    GLOBL(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    GNU_ATTRIBUTE(disabled = true),
    HIDDEN(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
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
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
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
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Optional {
            Seq(Dir(".else"), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
        }, Dir(".endif"))
    }),
    IFNOTDEF(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif")))
        }, Repeatable {
            Seq(Dir(".elseif"), InSpecific(Token.Type.SYMBOL), Repeatable { Except(XOR(Dir(".endif"), Dir(".else"), Dir(".elseif"))) })
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
        Seq(SpecNode(GASNodeType.EXPRESSION_STRING), Optional {
            Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS), Optional {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        })
    }),
    INCLUDE(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    INT(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    INTERNAL(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
        })
    }),
    IRP(rule = Rule {
        Seq(
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
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            Optional { Except(XOR(Dir(".ENDR"), InSpecific(emulator.kit.assembler.lexer.Token.Type.LINEBREAK))) },
            Repeatable {
                Except(Dir(".ENDR"))
            }, Dir(".ENDR")
        )
    }),
    LCOMM(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
    }),
    LFLAGS,
    LINE(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    LINKONCE(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    LIST,
    LN(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    LOC(disabled = true),
    LOC_MARK_LABELS(disabled = true),
    LOCAL(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
        })
    }),
    LONG(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    MACRO(rule = Rule(ignoreSpace = false) {
        Seq(
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
        Seq(InSpecific(Token.Type.SYMBOL), Repeatable {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
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
        InSpecific(Token.Type.SYMBOL)
    }),
    PUSHSECTION(rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
            Optional {
                Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
            }
        )
    }),
    QUAD(rule = Rule {
        Optional {
            Seq(emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.assembler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    RODATA(isSection = true, rule = Rule {
        Optional {
            emulator.kit.assembler.Rule.Component.InSpecific(emulator.kit.assembler.lexer.Token.Type.SYMBOL)
        }
    }),
    RELOC(rule = Rule {
        Seq(
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    REPT(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    SBTTL(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    SCL(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    SECTION(isSection = true, rule = Rule {
        XOR(Dir(".DATA"), Dir(".TEXT"), Dir(".RODATA"), Dir(".BSS"))
    }),
    SET(rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
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
            Seq(emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.assembler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    SPACE(rule = Rule {
        Seq(
            emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS),
            emulator.kit.assembler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            }
        )
    }),
    STABD,
    STABN,
    STABS,
    STRING(rule = Rule {
        Seq(
            emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.assembler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING8(rule = Rule {
        Seq(
            emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.assembler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRING16(rule = Rule {
        Seq(
            emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING),
            emulator.kit.assembler.Rule.Component.Optional {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_STRING))
            }
        )
    }),
    STRUCT(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    SUBSECTION(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    SYMVER,
    TAG(rule = Rule {
        InSpecific(Token.Type.SYMBOL)
    }),
    TEXT(rule = Rule {
        Optional {
            InSpecific(Token.Type.SYMBOL)
        }
    }),
    TITLE(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    TLS_COMMON(rule = Rule {
        Seq(
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
    TYPE,
    ULEB128(rule = Rule {
        Optional {
            Seq(emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS), emulator.kit.assembler.Rule.Component.Repeatable {
                Seq(Specific(","), emulator.kit.assembler.Rule.Component.SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    VAL(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    VERSION(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    VTABLE_ENTRY(rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            SpecNode(GASNodeType.EXPRESSION_ABS)
        )
    }),
    VTABLE_INHERIT(rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    WARNING(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_STRING)
    }),
    WEAK(rule = Rule {
        Seq(InSpecific(Token.Type.SYMBOL), emulator.kit.assembler.Rule.Component.Repeatable {
            Seq(Specific(","), InSpecific(Token.Type.SYMBOL))
        })
    }),
    WEAKREF(rule = Rule {
        Seq(
            InSpecific(Token.Type.SYMBOL),
            Specific(","),
            InSpecific(Token.Type.SYMBOL)
        )
    }),
    WORD(rule = Rule {
        Optional {
            Seq(SpecNode(GASNodeType.EXPRESSION_ABS), Repeatable {
                Seq(Specific(","), SpecNode(GASNodeType.EXPRESSION_ABS))
            })
        }
    }),
    ZERO(rule = Rule {
        SpecNode(GASNodeType.EXPRESSION_ABS)
    }),
    _2BYTE(rule = Rule {
        Seq(
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
            SpecNode(GASNodeType.EXPRESSION_ABS),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(GASNodeType.EXPRESSION_ABS)
                )
            }
        )
    });

    override fun getDetectionString(): String = this.name.removePrefix("_")

    override fun buildDirectiveContent(dirName: Token, tokens: List<Token>, allDirs: List<DirTypeInterface>,definedAssembly: DefinedAssembly): GASNode.Directive? {
        val type = dirName.dir
        if(type == null){
            dirName.addSeverity(Severity(Severity.Type.ERROR, "Missing linked directive type!"))
            return null
        }
        val result = this.rule.matchStart(tokens, allDirs,definedAssembly)
        if (result.matches) {
            return GASNode.Directive(type, dirName, result.matchingTokens, result.matchingNodes)
        }
        return null
    }

    companion object {
        val EMPTY = Rule()
    }

}