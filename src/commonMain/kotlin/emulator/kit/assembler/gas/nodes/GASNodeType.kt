package emulator.kit.assembler.gas.nodes

import emulator.kit.assembler.Rule

enum class GASNodeType() {
    ROOT,
    STATEMENT,
    DIRECTIVE,
    INSTRUCTION,
    LABEL,
    EXPRESSION_ABS,
    EXPRESSION_STRING,
    EXPRESSION_ANY
}