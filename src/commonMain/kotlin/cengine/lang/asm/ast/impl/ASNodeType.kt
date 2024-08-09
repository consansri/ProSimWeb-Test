package cengine.lang.asm.ast.impl

import emulator.kit.assembler.syntax.Rule

enum class ASNodeType(val rule: Rule? = null) {
    INT_EXPR,
    STRING_EXPR,
    ANY_EXPR,
    LABEL,
    ARG,
    ARG_DEF,
    INSTRUCTION,
    DIRECTIVE,
    STATEMENT,
    PROGRAM,
}