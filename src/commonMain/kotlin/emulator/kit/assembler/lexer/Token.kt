package emulator.kit.assembler.lexer

import emulator.kit.common.RegContainer
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface

class Token(val type: Type, val lineLoc: LineLoc, val content: String, val id: Int, val reg: RegContainer.Register? = null, val dir: DirTypeInterface? = null, val instr: InstrTypeInterface? = null) {
    var hlContent = content
    private var isPrefix = false
    private var severities: MutableList<Severity> = mutableListOf()
    private var codeStyle: CodeStyle? = CodeStyle.BASE0

    init {
        isPrefix = type == Type.COMPLEMENT
        if(type.style != null){
            hl(type.style)
        }
    }

    fun lowerOrEqualPrecedenceAs(other: Token): Boolean {
        val thisPrecedence = getPrecedence() ?: return false
        val otherPrecedence = getPrecedence() ?: return false
        return thisPrecedence.ordinal <= otherPrecedence.ordinal
    }

    fun isPrefix(): Boolean = isPrefix

    fun markAsPrefix() {
        isPrefix = true
    }

    fun getPrecedence(): Precedence? {
        return when (type) {
            Type.COMPLEMENT -> Precedence.PREFIX
            Type.MULT -> Precedence.HIGH
            Type.DIV -> Precedence.HIGH
            Type.REM -> Precedence.HIGH
            Type.SHL -> Precedence.HIGH
            Type.SHR -> Precedence.HIGH
            Type.BITWISE_OR -> Precedence.INTERMEDIATE
            Type.BITWISE_AND -> Precedence.INTERMEDIATE
            Type.BITWISE_XOR -> Precedence.INTERMEDIATE
            Type.BITWISE_ORNOT -> Precedence.INTERMEDIATE
            Type.PLUS -> if (isPrefix) Precedence.PREFIX else Precedence.LOW
            Type.MINUS -> if (isPrefix) Precedence.PREFIX else Precedence.LOW
            else -> null
        }
    }

    enum class Type(
        val regex: Regex? = null,
        val style: CodeStyle? = null,
        val isOperator: Boolean = false,
        val couldBePrefix: Boolean = false,
        val isPunctuation: Boolean = false,
        val isOpeningBracket: Boolean = false,
        val isClosingBracket: Boolean = false,
        val isNumberLiteral: Boolean = false,
        val isStringLiteral: Boolean = false,
        val isCharLiteral: Boolean = false
    ) {
        WHITESPACE(Regex("""^[\t ]+""")),
        LINEBREAK(Regex("^(\\r\\n|\\r|\\n)")),
        COMMENT_SL(Regex("^//.*"), CodeStyle.comment),
        COMMENT_ML(Regex("""^/\*([^*]|\*+[^*/])*\*/"""), CodeStyle.comment),
        DIRECTIVE(style = CodeStyle.keyWordDir),
        REGISTER(style = CodeStyle.keyWordReg),
        INSTRNAME(style = CodeStyle.keyWordInstr),
        INT_BIN(Regex("^${Regex.escape(Settings.PRESTRING_BINARY)}([01]+)", RegexOption.IGNORE_CASE), CodeStyle.integer,  isNumberLiteral = true),
        INT_HEX(Regex("^${Regex.escape(Settings.PRESTRING_HEX)}([0-9a-f]+)", RegexOption.IGNORE_CASE), CodeStyle.integer,  isNumberLiteral = true),
        INT_OCT(Regex("^${Regex.escape(Settings.PRESTRING_OCT)}([0-7]+)", RegexOption.IGNORE_CASE), CodeStyle.integer,  isNumberLiteral = true),
        INT_DEC(Regex("^${Regex.escape(Settings.PRESTRING_DECIMAL)}([0-9]+)", RegexOption.IGNORE_CASE), CodeStyle.integer,  isNumberLiteral = true),
        STRING_ML(Regex("^\"\"\"(?:\\.|[^\"])*\"\"\""),CodeStyle.string, isStringLiteral = true),
        STRING_SL(Regex("""^"(\\.|[^\\"])*""""), CodeStyle.string,isStringLiteral = true),
        CHAR(Regex("""^'(\\.|[^\\'])'"""), CodeStyle.char, isCharLiteral = true),
        SYMBOL(Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")),
        ARG_REF(Regex("""^\\[a-zA-Z$._][a-zA-Z0-9$._]*"""), CodeStyle.argument),
        ARG_SEPARATOR(Regex("""^\\\(\)"""),CodeStyle.argument),
        ASSIGNMENT(Regex("^=")),
        COMPLEMENT(Regex("^~"), isOperator = true),
        MULT(Regex("^\\*"), isOperator = true),
        DIV(Regex("^/"), isOperator = true),
        REM(Regex("^%"), isOperator = true),
        SHL(Regex("^<<"), isOperator = true),
        SHR(Regex("^>>"), isOperator = true),
        BITWISE_OR(Regex("^\\|"), isOperator = true),
        BITWISE_AND(Regex("^&"), isOperator = true),
        BITWISE_XOR(Regex("^\\^"), isOperator = true),
        BITWISE_ORNOT(Regex("^!"), isOperator = true),
        PLUS(Regex("^\\+"), isOperator = true, couldBePrefix = true),
        MINUS(Regex("^-"), isOperator = true, couldBePrefix = true),
        COMMA(Regex("^,"), isPunctuation = true),
        SEMICOLON(Regex("^;"), isPunctuation = true),
        COLON(Regex("^:"), isPunctuation = true),
        BRACKET_OPENING(Regex("^\\("), isPunctuation = true, isOpeningBracket = true),
        BRACKET_CLOSING(Regex("^\\)"), isPunctuation = true, isClosingBracket = true),
        CURLY_BRACKET_OPENING(Regex("^${Regex.escape("{")}"), isPunctuation = true, isOpeningBracket = true),
        CURLY_BRACKET_CLOSING(Regex("^${Regex.escape("}")}"), isPunctuation = true, isClosingBracket = true),
        SQUARE_BRACKET_OPENING(Regex("^\\["), isPunctuation = true, isOpeningBracket = true),
        SQUARE_BRACKET_CLOSING(Regex("^${Regex.escape("]")}"), isPunctuation = true, isClosingBracket = true),
        COMMENT_NATIVE(Regex("^#.+"), CodeStyle.comment),
        ERROR(Regex("^."));

        fun isLiteral(): Boolean = isStringLiteral || isNumberLiteral || isCharLiteral
        fun isBasicBracket(): Boolean = this == BRACKET_OPENING || this == BRACKET_CLOSING
    }

    enum class BracketPairs(val opening: Type, val closing: Type) {
        BASIC(Type.BRACKET_OPENING, Type.BRACKET_CLOSING),
        CURLY(Type.CURLY_BRACKET_OPENING, Type.CURLY_BRACKET_CLOSING),
        SQUARE(Type.SQUARE_BRACKET_OPENING, Type.SQUARE_BRACKET_CLOSING)
    }

    enum class Precedence {
        LOWEST,
        LOW,
        INTERMEDIATE,
        HIGH,
        PREFIX,
    }

    fun printError(): String? {
        val errors = severities.filter { it.type == Severity.Type.ERROR }
        if (errors.isEmpty()) return null
        val errorString = errors.joinToString("\n\t") { it.message }
        return "Error at $lineLoc {${this::class.simpleName}:${this.content}} $errorString"
    }

    fun printWarning(): String? {
        val warnings = severities.filter { it.type == Severity.Type.WARNING }
        if (warnings.isEmpty()) return null
        val warningString = warnings.joinToString("\n\t") { it.message }
        return "Warning at $lineLoc $warningString"
    }

    fun isPseudo(): Boolean {
        return id < 0
    }

    fun addSeverity(type: Severity.Type, message: String){
        this.severities.add(Severity(type, message))
    }

    fun addSeverity(severity: Severity) {
        this.severities.add(severity)
    }

    fun removeSeverityIfError() {
        val buffered = ArrayList(severities)
        severities.clear()
        severities.addAll(buffered.filter { it.type != Severity.Type.ERROR })
    }

    fun hl(vararg codeStyle: CodeStyle) {
        this.codeStyle = codeStyle.firstOrNull()
    }

    fun getCodeStyle() = codeStyle
    fun getMajorSeverity() = severities.firstOrNull { it.type == Severity.Type.ERROR } ?: severities.firstOrNull { it.type == Severity.Type.WARNING } ?: severities.firstOrNull { it.type == Severity.Type.INFO }
    fun getSeverities(): List<Severity> = severities

    override fun toString(): String = content
    data class LineLoc(val fileName: String, var lineID: Int, val startIndex: Int, val endIndex: Int) {
        override fun toString(): String {
            return "$fileName[line ${lineID + 1}]:"
        }
    }
}