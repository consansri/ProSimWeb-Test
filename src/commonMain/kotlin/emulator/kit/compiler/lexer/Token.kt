package emulator.kit.compiler.lexer

import emulator.kit.common.RegContainer
import emulator.kit.compiler.CodeStyle
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.nativeLog
import emulator.kit.types.Variable

sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {
    var hlContent = content
    private var severities: MutableList<Severity> = mutableListOf()
    private var codeStyle: CodeStyle? = CodeStyle.BASE0

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

    fun addSeverity(severity: Severity) {
        this.severities.add(severity)
    }

    fun removeSeverityIfError() {
        val buffered = ArrayList(severities)
        severities.clear()
        severities.addAll(buffered.filter { it.type != Severity.Type.ERROR })
    }

    open fun hl(vararg codeStyle: CodeStyle) {
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

    class LINEBREAK(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        companion object {
            val REGEX = Regex("^(\\r\\n|\\r|\\n)")
        }
    }

    class SPACE(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        companion object {
            val REGEX = Regex("""^[\t ]+""")
        }
    }

    class COMMENT(val type: CommentType, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        init {
            hl(CodeStyle.comment)
        }

        enum class CommentType(val regex: Regex) {
            SINGLELINE(Regex("^//.*\n")),
            MULTILINE(Regex("""^/\*([^*]|\*+[^*/])*\*/"""))
        }
    }

    sealed class KEYWORD(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        class Register(val register: RegContainer.Register, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            init {
                hl(CodeStyle.keyWordReg)
            }
        }

        class InstrName(val instrType: InstrTypeInterface, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            init {
                hl(CodeStyle.keyWordInstr)
            }
        }

        class Directive(val dirType: DirTypeInterface, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            init {
                hl(CodeStyle.keyWordDir)
            }
        }
    }

    sealed class LABEL(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        init {
            this.hl(CodeStyle.label)
        }

        class Local(val identifier: Int, lineLoc: LineLoc, content: String, id: Int) : LABEL(lineLoc, content, id) {
            companion object {
                val REGEX = Regex("^[0-9]*:")
            }
        }

        class Basic(lineLoc: LineLoc, content: String, id: Int) : LABEL(lineLoc, content, id) {
            companion object {
                val REGEX = Regex("^[a-zA-Z\$._][a-zA-Z0-9\$._]*:")
            }

            val identifier: String = content.removeSuffix(":")
        }
    }

    class SYMBOL(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {

        companion object {
            val REGEX = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
        }

        var value: Variable.Value? = null
    }

    sealed class LITERAL(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {

        abstract fun getValue(size: Variable.Size? = null): Variable.Value
        sealed class NUMBER(lineLoc: LineLoc, content: String, id: Int) : LITERAL(lineLoc, content, id) {
            class INTEGER(val format: IntegerFormat, val matchResult: MatchResult, lineLoc: LineLoc, content: String, id: Int) : NUMBER(lineLoc, content, id) {

                val digits: String

                init {
                    hl(CodeStyle.integer)
                    digits = matchResult.groupValues.getOrNull(1) ?: content
                }

                enum class IntegerFormat(val regex: Regex) {
                    BIN(Regex("^${Regex.escape(Settings.PRESTRING_BINARY)}([01]+)", RegexOption.IGNORE_CASE)),
                    HEX(Regex("^${Regex.escape(Settings.PRESTRING_HEX)}([0-9a-f]+)", RegexOption.IGNORE_CASE)),
                    OCT(Regex("^${Regex.escape(Settings.PRESTRING_OCT)}([0-7]+)", RegexOption.IGNORE_CASE)),
                    DEC(Regex("^${Regex.escape(Settings.PRESTRING_DECIMAL)}([0-9]+)", RegexOption.IGNORE_CASE)),
                }

                override fun getValue(size: Variable.Size?): Variable.Value {
                    return when (format) {
                        IntegerFormat.BIN -> if (size != null) Variable.Value.Bin(digits, size) else Variable.Value.Bin(digits)
                        IntegerFormat.HEX -> if (size != null) Variable.Value.Hex(digits, size) else Variable.Value.Hex(digits)
                        IntegerFormat.OCT -> if (size != null) Variable.Value.Bin(digits.toInt(8).toString(2), size) else Variable.Value.Bin(digits.toInt(8).toString(2))
                        IntegerFormat.DEC -> if (size != null) Variable.Value.Dec(digits, size) else Variable.Value.Dec(digits)
                    }
                }
            }
        }

        sealed class CHARACTER(lineLoc: LineLoc, content: String, id: Int) : LITERAL(lineLoc, content, id) {
            abstract val rawContent: String

            class STRING(val type: StringType, val matchResult: MatchResult, lineLoc: LineLoc, content: String, id: Int) : CHARACTER(lineLoc, content, id) {

                override val rawContent: String = when (type) {
                    StringType.SINGLELINE -> content.substring(1, content.length - 1)
                    StringType.MULTILINE -> content.substring(3, content.length - 3)
                }

                init {
                    hl(CodeStyle.string)
                }

                enum class StringType(val regex: Regex) {
                    SINGLELINE(Regex("""^"(\\.|[^\\"])*"""")),
                    MULTILINE(Regex("^\"\"\"(?:\\.|[^\"])*\"\"\""))
                }

                override fun getValue(size: Variable.Size?): Variable.Value {
                    val stringChars = rawContent.map { it.code.toString(16) }.joinToString("") { it }
                    return if (size != null) Variable.Value.Hex(stringChars, size) else Variable.Value.Hex(stringChars)
                }
            }

            class CHAR(val matchResult: MatchResult, lineLoc: LineLoc, content: String, id: Int) : CHARACTER(lineLoc, content, id) {

                override val rawContent: String = content.substring(1, content.length - 1)

                companion object {
                    val REGEX = Regex("""'(\\.|[^\\'])'""")
                }

                init {
                    hl(CodeStyle.char)
                }

                override fun getValue(size: Variable.Size?): Variable.Value {
                    val stringChars = rawContent.map { it.code.toString(16) }.joinToString("") { it }
                    return if (size != null) Variable.Value.Hex(stringChars, size) else Variable.Value.Hex(stringChars)
                }
            }
        }
    }

    class OPERATOR(val operatorType: OperatorType, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {

        private var isPrefix = false

        init {
            hl(CodeStyle.operator)
            isPrefix = operatorType == OperatorType.COMPLEMENT
        }

        fun lowerOrEqualPrecedenceAs(other: OPERATOR): Boolean {
            return getPrecedence().ordinal <= other.getPrecedence().ordinal
        }

        fun isPrefix(): Boolean = isPrefix

        fun markAsPrefix() {
            isPrefix = true
        }

        fun getPrecedence(): Precedence {
            return when (operatorType) {
                OperatorType.COMPLEMENT -> Precedence.PREFIX
                OperatorType.MULT -> Precedence.HIGH
                OperatorType.DIV -> Precedence.HIGH
                OperatorType.REM -> Precedence.HIGH
                OperatorType.SHL -> Precedence.HIGH
                OperatorType.SHR -> Precedence.HIGH
                OperatorType.BITWISE_OR -> Precedence.INTERMEDIATE
                OperatorType.BITWISE_AND -> Precedence.INTERMEDIATE
                OperatorType.BITWISE_XOR -> Precedence.INTERMEDIATE
                OperatorType.BITWISE_ORNOT -> Precedence.INTERMEDIATE
                OperatorType.PLUS -> if (isPrefix) Precedence.PREFIX else Precedence.LOW
                OperatorType.MINUS -> if (isPrefix) Precedence.PREFIX else Precedence.LOW
            }
        }

        enum class OperatorType(val regex: Regex, val couldBePrefix: Boolean = false) {
            COMPLEMENT(Regex("^~")),
            MULT(Regex("^\\*")),
            DIV(Regex("^/")),
            REM(Regex("^%")),
            SHL(Regex("^<<")),
            SHR(Regex("^>>")),
            BITWISE_OR(Regex("^\\|")),
            BITWISE_AND(Regex("^&")),
            BITWISE_XOR(Regex("^\\^")),
            BITWISE_ORNOT(Regex("^!")),
            PLUS(Regex("^\\+"), true),
            MINUS(Regex("^-"), true)
        }

        enum class Precedence {
            LOWEST,
            LOW,
            INTERMEDIATE,
            HIGH,
            PREFIX,
        }
    }

    class PUNCTUATION(val type: PunctuationType, lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        init {
            hl(CodeStyle.punctuation)
        }

        fun isAnyBracket(): Boolean {
            return when (type) {
                PunctuationType.DOT -> false
                PunctuationType.COMMA -> false
                PunctuationType.SEMICOLON -> false
                PunctuationType.COLON -> false
                PunctuationType.BRACKET_OPENING -> true
                PunctuationType.BRACKET_CLOSING -> true
                PunctuationType.CURLY_BRACKET_OPENING -> true
                PunctuationType.CURLY_BRACKET_CLOSING -> true
                PunctuationType.SQUARE_BRACKET_OPENING -> true
                PunctuationType.SQUARE_BRACKET_CLOSING -> true
            }
        }

        fun isBasicBracket(): Boolean {
            return type == PunctuationType.BRACKET_OPENING || type == PunctuationType.BRACKET_CLOSING
        }

        fun isOpening(): Boolean {
            BracketPairs.entries.forEach {
                if (type == it.opening) return true
            }
            return false
        }

        fun isClosing(): Boolean {
            BracketPairs.entries.forEach {
                if (type == it.closing) return true
            }
            return false
        }

        enum class PunctuationType(val regex: Regex) {
            DOT(Regex("^\\.")),
            COMMA(Regex("^,")),
            SEMICOLON(Regex("^;")),
            COLON(Regex("^:")),
            BRACKET_OPENING(Regex("^\\(")),
            BRACKET_CLOSING(Regex("^\\)")),
            CURLY_BRACKET_OPENING(Regex("^${Regex.escape("{")}")),
            CURLY_BRACKET_CLOSING(Regex("^${Regex.escape("}")}")),
            SQUARE_BRACKET_OPENING(Regex("^\\[")),
            SQUARE_BRACKET_CLOSING(Regex("^${Regex.escape("]")}")),
        }

        enum class BracketPairs(val opening: PunctuationType, val closing: PunctuationType) {
            BASIC(PunctuationType.BRACKET_OPENING, PunctuationType.BRACKET_CLOSING),
            CURLY(PunctuationType.CURLY_BRACKET_OPENING, PunctuationType.CURLY_BRACKET_CLOSING),
            SQUARE(PunctuationType.SQUARE_BRACKET_OPENING, PunctuationType.SQUARE_BRACKET_CLOSING)
        }
    }

    class ANYCHAR(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        companion object {
            val REGEX = Regex("^.")
        }
    }

    class ERROR(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
        init {
            hl(CodeStyle.error)
        }
    }
}