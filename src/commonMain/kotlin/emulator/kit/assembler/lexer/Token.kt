package emulator.kit.assembler.lexer

import emulator.kit.assembler.AsmFile
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.lexer.Token.LineLoc
import emulator.kit.common.RegContainer

/**
 * [Token]s will be created by the [Lexer] and further be used by the Parser to create Nodes out of it.
 * @property type identificates [Token.Type].
 * @property lineLoc identificates the file location ([LineLoc]) of the token.
 * @property id contains an [Int] from which the place in a sequence of multiple tokens always can be determined.
 * @property isPseudoOf when creating tokens from pseudo content it is important to link them to a real token to display severities the right way.
 * @property severities a [Token] can hold multiple Messages.
 * @property codeStyle holds the style information for Editors.
 */
class Token(val type: Type, val lineLoc: LineLoc, val content: String, val id: Int, val onlyNumber: String = "", val reg: RegContainer.Register? = null, val dir: DirTypeInterface? = null, val instr: InstrTypeInterface? = null, var isPseudoOf: Token? = null) {
    private var isPrefix = false
    private var severities: MutableList<Severity> = mutableListOf()
    private var codeStyle: CodeStyle = CodeStyle.BASE0

    init {
        isPrefix = type == Type.COMPLEMENT
        if (type.style != null) {
            hl(type.style)
        }
    }

    /**
     * Gets the highlighted content of the token.
     */
    fun getHL(): List<Pair<String, CodeStyle>> {
        return when (type) {
            Type.STRING_SL -> getEscapedHL()
            Type.STRING_ML -> getEscapedHL()
            Type.CHAR -> getEscapedHL()
            else -> listOf(content to codeStyle)
        }
    }

    /**
     * Gets the content of the token as a string.
     */
    fun getContentAsString(): String = when (type) {
        Type.WHITESPACE -> ""
        Type.LINEBREAK -> "\n"
        Type.COMMENT_SL -> ""
        Type.COMMENT_ML -> ""
        Type.STRING_ML -> content.substring(3, content.length - 3).replaceEscapedChars()
        Type.STRING_SL -> content.substring(1, content.length - 1).replaceEscapedChars()
        Type.CHAR -> content.substring(1).replaceEscapedChars()
        Type.ARG_REF -> content.substring(1)
        Type.ARG_SEPARATOR -> ""
        Type.COMMENT_NATIVE -> ""
        Type.DIRECTIVE -> content.removePrefix(".").lowercase()
        else -> content
    }

    /**
     * Checks if the token has a higher or equal precedence compared to another token.
     */
    fun higherOrEqualPrecedenceAs(other: Token): Boolean {
        val thisPrecedence = getPrecedence() ?: return false
        val otherPrecedence = other.getPrecedence() ?: return false
        return thisPrecedence.ordinal >= otherPrecedence.ordinal
    }

    /**
     * Checks if the token is a prefix operator.
     */
    fun isPrefix(): Boolean = isPrefix

    /**
     * Marks the token as a prefix operator.
     */
    fun markAsPrefix() {
        isPrefix = true
    }

    /**
     * Gets the precedence of the token.
     */
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

    /**
     * Prints the error message associated with the token.
     */
    fun printError(): String? {
        val errors = severities.filter { it.type == Severity.Type.ERROR }
        if (errors.isEmpty()) return null
        val errorString = errors.joinToString("\n\t") { it.message }
        return "$lineLoc Error: {${this.type} ${this.content}} $errorString"
    }

    /**
     * Prints the warning message associated with the token.
     */
    fun printWarning(): String? {
        val warnings = severities.filter { it.type == Severity.Type.WARNING }
        if (warnings.isEmpty()) return null
        val warningString = warnings.joinToString("\n\t") { it.message }
        return "$lineLoc Warning: $warningString"
    }

    /**
     * Adds a severity to the token.
     */
    fun addSeverity(type: Severity.Type, message: String) {
        this.severities.add(Severity(type, message))
        isPseudoOf?.addSeverity(type, message)
    }

    /**
     * Adds a severity to the token.
     */
    fun addSeverity(severity: Severity) {
        this.severities.add(severity)
        isPseudoOf?.addSeverity(severity)
    }

    /**
     * Removes the severity if it is an error.
     */
    fun removeSeverityIfError() {
        val buffered = ArrayList(severities)
        severities.clear()
        severities.addAll(buffered.filter { it.type != Severity.Type.ERROR })
        isPseudoOf?.removeSeverityIfError()
    }

    /**
     * Highlights the token using the provided code style.
     */
    fun hl(codeStyle: CodeStyle) {
        this.codeStyle = codeStyle
    }

    /**
     * Gets the code style of the token.
     */
    fun getCodeStyle() = codeStyle

    /**
     * Gets the major severity of the token.
     */
    fun getMajorSeverity() = severities.firstOrNull { it.type == Severity.Type.ERROR } ?: severities.firstOrNull { it.type == Severity.Type.WARNING } ?: severities.firstOrNull { it.type == Severity.Type.INFO }

    /**
     * Gets all severities associated with the token.
     */
    fun getSeverities(): List<Severity> = severities

    private fun String.replaceEscapedChars(): String {
        var result = this
        EscapedChar.entries.forEach {
            result = result.replace(it.id, it.replacement)
        }
        return result
    }

    private fun getEscapedHL(): List<Pair<String, CodeStyle>> {
        val result = mutableListOf<Pair<String, CodeStyle>>()
        var startIndex = 0

        var noMatchContent = ""
        while (startIndex < content.length) {
            var foundEscape = false
            for (reg in EscapedChar.entries) {
                val match = reg.regex.find(content.substring(startIndex))
                if (match != null) {
                    if (noMatchContent.isNotEmpty()) {
                        result.add(noMatchContent to codeStyle)
                        noMatchContent = ""
                    }
                    result.add(match.value to CodeStyle.escape)
                    startIndex += match.value.length
                    foundEscape = true
                    break
                }
            }
            if (foundEscape) continue

            noMatchContent += content[startIndex]
            startIndex++
        }

        if (noMatchContent.isNotEmpty()) {
            result.add(noMatchContent to codeStyle)
        }

        return result
    }

    override fun toString(): String = content

    /**
     * Represents the type of a token.
     */
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
        COMMENT_NATIVE(Regex("^#.+"), CodeStyle.comment),
        DIRECTIVE(style = CodeStyle.keyWordDir),
        REGISTER(style = CodeStyle.keyWordReg),
        INSTRNAME(style = CodeStyle.keyWordInstr),
        SYMBOL(Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")),
        L_LABEL_REF(Regex("^[0-9]+[bf](?![0-9a-f])", RegexOption.IGNORE_CASE)),
        INT_BIN(Regex("^${Regex.escape(Settings.PRESTRING_BINARY)}([01]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
        INT_HEX(Regex("^${Regex.escape(Settings.PRESTRING_HEX)}([0-9a-f]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
        INT_OCT(Regex("^${Regex.escape(Settings.PRESTRING_OCT)}([0-7]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
        INT_DEC(Regex("^${Regex.escape(Settings.PRESTRING_DECIMAL)}([0-9]+)", RegexOption.IGNORE_CASE), CodeStyle.integer, isNumberLiteral = true),
        STRING_ML(Regex("^\"\"\"(?:\\.|[^\"])*\"\"\""), CodeStyle.string, isStringLiteral = true),
        STRING_SL(Regex("""^"(\\.|[^\\"])*""""), CodeStyle.string, isStringLiteral = true),
        CHAR(Regex("""^'(\\.|[^\\'])"""), CodeStyle.char, isCharLiteral = true),
        ARG_REF(Regex("""^\\[a-zA-Z$._][a-zA-Z0-9$._]*"""), CodeStyle.argument),
        ARG_SEPARATOR(Regex("""^\\\(\)"""), CodeStyle.argument),
        ASSIGNMENT(Regex("^="), CodeStyle.operator),
        COMPLEMENT(Regex("^~"), CodeStyle.operator, isOperator = true),
        MULT(Regex("^\\*"), CodeStyle.operator, isOperator = true),
        DIV(Regex("^/"), CodeStyle.operator, isOperator = true),
        REM(Regex("^%"), CodeStyle.operator, isOperator = true),
        SHL(Regex("^<<"), CodeStyle.operator, isOperator = true),
        SHR(Regex("^>>"), CodeStyle.operator, isOperator = true),
        BITWISE_OR(Regex("^\\|"), CodeStyle.operator, isOperator = true),
        BITWISE_AND(Regex("^&"), CodeStyle.operator, isOperator = true),
        BITWISE_XOR(Regex("^\\^"), CodeStyle.operator, isOperator = true),
        BITWISE_ORNOT(Regex("^!"), CodeStyle.operator, isOperator = true),
        PLUS(Regex("^\\+"), CodeStyle.operator, isOperator = true, couldBePrefix = true),
        MINUS(Regex("^-"), CodeStyle.operator, isOperator = true, couldBePrefix = true),
        COMMA(Regex("^,"), CodeStyle.punctuation, isPunctuation = true),
        SEMICOLON(Regex("^;"), CodeStyle.punctuation, isPunctuation = true),
        COLON(Regex("^:"), CodeStyle.punctuation, isPunctuation = true),
        BRACKET_OPENING(Regex("^\\("), CodeStyle.punctuation, isPunctuation = true, isOpeningBracket = true),
        BRACKET_CLOSING(Regex("^\\)"), CodeStyle.punctuation, isPunctuation = true, isClosingBracket = true),
        CURLY_BRACKET_OPENING(Regex("^${Regex.escape("{")}"), CodeStyle.punctuation, isPunctuation = true, isOpeningBracket = true),
        CURLY_BRACKET_CLOSING(Regex("^${Regex.escape("}")}"), CodeStyle.punctuation, isPunctuation = true, isClosingBracket = true),
        SQUARE_BRACKET_OPENING(Regex("^\\["), CodeStyle.punctuation, isPunctuation = true, isOpeningBracket = true),
        SQUARE_BRACKET_CLOSING(Regex("^${Regex.escape("]")}"), CodeStyle.punctuation, isPunctuation = true, isClosingBracket = true),
        ERROR(Regex("^."));

        fun isLinkableSymbol(): Boolean = this == SYMBOL || this == L_LABEL_REF
        fun isLiteral(): Boolean = isStringLiteral || isNumberLiteral || isCharLiteral
        fun isBasicBracket(): Boolean = this == BRACKET_OPENING || this == BRACKET_CLOSING
    }

    /**
     * Represents pairs of opening and closing brackets.
     */
    enum class BracketPairs(val opening: Type, val closing: Type) {
        BASIC(Type.BRACKET_OPENING, Type.BRACKET_CLOSING),
        CURLY(Type.CURLY_BRACKET_OPENING, Type.CURLY_BRACKET_CLOSING),
        SQUARE(Type.SQUARE_BRACKET_OPENING, Type.SQUARE_BRACKET_CLOSING)
    }

    /**
     * Represents the precedence of a token.
     */
    enum class Precedence {
        LOWEST,
        LOW,
        INTERMEDIATE,
        HIGH,
        PREFIX,
    }

    /**
     * Represents the location of the [Token].
     */
    data class LineLoc(val file: AsmFile, var lineID: Int, val startIndex: Int, val endIndex: Int) {
        override fun toString(): String {
            return "${file.mainRelativeName}[line ${lineID + 1}]"
        }
    }

    /**
     * Represents escaped characters.
     */
    enum class EscapedChar(val id: String, val replacement: String) {
        N("\\n", "\n"),
        T("\\t", "\t"),
        B("\\b", "\b"),
        R("\\r", "\r"),
        F("\\f", "\u000c"),
        II("\\\"", "\""),
        I("\\\'", "\'"),
        SLASH("\\\\", "\\");

        val regex: Regex = Regex("^${Regex.escape(id)}")
    }
}