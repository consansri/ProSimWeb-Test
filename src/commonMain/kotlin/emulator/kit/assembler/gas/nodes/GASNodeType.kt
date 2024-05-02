package emulator.kit.assembler.gas.nodes

import emulator.kit.assembler.Rule

enum class GASNodeType(val rule: Rule? = null) {
    EXPRESSION_INTEGER,
    EXPRESSION_STRING,
    EXPRESSION_ANY,
    LABEL,
    ARGUMENT,
    INSTRUCTION,
    DIRECTIVE,
    STATEMENT,
    ROOT,

}