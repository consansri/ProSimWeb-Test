package emulator.kit.compiler.lexer

import emulator.kit.compiler.CodeStyle

data class Severity(val type: Type, val message: String){
    enum class Type(val codeStyle: CodeStyle) {
        ERROR(CodeStyle.RED),
        WARNING(CodeStyle.YELLOW),
        INFO(CodeStyle.BASE4)
    }
}