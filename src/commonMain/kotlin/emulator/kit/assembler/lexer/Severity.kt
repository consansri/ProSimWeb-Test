package emulator.kit.assembler.lexer

import emulator.kit.assembler.CodeStyle

data class Severity(val type: Type, val message: String){

    companion object{
        const val MSG_MISSING_LINEBREAK = "Missing Line Break!"
        const val MSG_NOT_A_STATEMENT = "Expected a statement!"
        const val MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERATOR = "Expected an Operator!"
        const val MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERAND = "Expected an Operand!"
        const val MSG_TRAILING_COMMA = "List is ending on a trailing comma!"
    }

    override fun toString(): String {
        return "${type.name} $message"
    }
    enum class Type(val codeStyle: CodeStyle) {
        ERROR(CodeStyle.RED),
        WARNING(CodeStyle.YELLOW),
        INFO(CodeStyle.BASE4)
    }
}