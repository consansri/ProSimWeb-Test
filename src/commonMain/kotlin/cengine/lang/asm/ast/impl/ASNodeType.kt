package cengine.lang.asm.ast.impl

enum class ASNodeType {
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