package emulator.kit.compiler.gas.nodes

enum class GASNodeType {
    ROOT,
    STATEMENT,
    DIRECTIVE,
    INSTRUCTION,
    LABEL,
    IDENTIFIER,
    EXPRESSION_ABS,
    EXPRESSION_STRING,
    EXPRESSION_ANY
}