package emulator.kit.assembly

import Settings
import kotlin.time.measureTime
import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.assembly.Syntax.TokenSeq
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.nativeError
import emulator.kit.nativeLog
import kotlinx.datetime.*

/**
 * The [Compiler] is the first instance which analyzes the text input. Common pre analyzed tokens will be delivered to each Syntax implementation. The [Compiler] fires the compilation events in the following order.
 *
 * 1. common analysis ([tokenize])
 * 2. specific analysis ([parse] which uses the given logic from [syntax])
 * 3. highlight tokens ([highlight])
 * 4. convert syntax tree to binary ([assemble] which uses the given logic from [assembly])
 *
 * @param syntax gets an object of the architecture specific [Syntax]-Class implementation from the assembler configuration through the [Architecture].
 * @param assembly gets an object of the architecture specific [Assembly]-Class implementation from the assembler configuration through the [Architecture].
 * @param hlFlagCollection contains the standard token highlighting flags.
 *
 * @property regexCollection contains the standard token regular expressions.
 *
 */
class NewCompiler(
    private val architecture: Architecture,
    private val syntax: Syntax,
    private val assembly: Assembly,
    private val prefixes: ConstantPrefixes,
    private val detectRegisters: Boolean
) {

    private var isBuildable = false
    private var pseudoID = -1
    private var assemblyMap: Assembly.AssemblyMap = Assembly.AssemblyMap()
    val processes: MutableList<Process> = mutableListOf()

    /**
     * Executes and controls the compilation
     */
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean = true): CompileResult {
        TODO()
    }

    /**
     * Get the State of the last compilation (true if no errors where found in grammar tree)
     */
    fun isBuildable(): Boolean = isBuildable

    /**
     * Map which holds the Address to LineID connection to allow ExeUntilLine Execution Event
     */
    fun getAssemblyMap(): Assembly.AssemblyMap = assemblyMap

    /**
     * Only reassembles the current Grammar Tree (no lexing, no parsing, no highlighting)
     */
    fun reassemble(file: CompilerFile) {
        TODO()
    }

    /**
     * Resets all local code states
     *
     * [dryContent]
     * [dryLines]
     * [tokenList]
     * [tokenLines]
     * [hlContent]
     */

    /**
     * Transforms the [dryContent] into a List of Compiler Tokens ([tokenList], [tokenLines])
     */
    private fun tokenize(file: CompilerFile): List<Token> {
        val tokenList = mutableListOf<Token>()
        var remaining = file.content
        var lineID = 0
        var startIndex = 0

        while (remaining.isNotEmpty()) {

        }

        tokenList.resolveExpressions()
        return tokenList
    }

    /**
     * Calls the specific [Syntax] check function which builds the [Syntax.SyntaxTree]
     */
    private fun parse(tokenList: List<Token>, others: List<CompilerFile>): Syntax.SyntaxTree {
        TODO()
    }

    /**
     * Calls the specific [Assembly.assemble] function which analyzes the [Syntax.SyntaxTree] and stores the resulting bytes into the Memory
     */
    private fun assemble(syntaxTree: Syntax.SyntaxTree) {
        if (isBuildable) {
            architecture.getMemory().clear()
            architecture.getRegContainer().pc.reset()
            architecture.getTranscript().clear()

            syntaxTree.let {
                val assembleTime = measureTime {
                    assemblyMap = assembly.assemble(architecture, it)
                }
                architecture.getConsole().compilerInfo("assembl\ttook ${assembleTime.inWholeMicroseconds}µs")
                val disassembleTime = measureTime {
                    assembly.disassemble(architecture)
                }
                architecture.getConsole().compilerInfo("disassembl\ttook ${disassembleTime.inWholeMicroseconds}µs")
            }
        }
    }

    /**
     * This function could be used to insert and [pseudoTokenize] custom inserted code such as macro inserts to get its resolving [Compiler.Token]'s from it.
     * This should only be called while building the [Syntax.SyntaxTree]!
     */
    fun pseudoTokenize(content: String, lineLoc: LineLoc): List<Token> {
        val pseudoTokens = mutableListOf<Token>()
        var remaining = content
        var startIndex = 0
        while (remaining.isNotEmpty()) {
            pseudoID -= 1

        }

        pseudoTokens.resolveExpressions()

        return pseudoTokens
    }

    /**
     * returns the current [Syntax.SyntaxTree]
     */
    fun getGrammarTree(file: CompilerFile): Syntax.SyntaxTree? = TODO() //syntax.treeCache[file]

    fun isInTreeCacheAndHasNoErrors(file: CompilerFile): Boolean = TODO() // syntax.treeCache[file] != null && syntax.treeCache[file]?.rootNode?.allErrors?.isEmpty() ?: true

    private fun MutableList<Token>.resolveExpressions() {
        TODO()
        /*while (true) {
            var foundExpression = false
            Token.Constant.Expression.ExpressionType.entries.forEach { type ->
                val result = type.tokenSeq.matches(*this.toTypedArray())
                if (result.matches) {
                    foundExpression = true
                    val matchedTokens = result.sequenceMap.map { it.token }
                    if (matchedTokens.isNotEmpty()) {
                        val first = matchedTokens.first()
                        val last = matchedTokens.last()
                        val lineLoc = if (first == last) {
                            first.lineLoc
                        } else {
                            LineLoc(first.lineLoc.fileName, first.lineLoc.lineID, first.lineLoc.startIndex, last.lineLoc.endIndex)
                        }

                        val expression = Token.Constant.Expression(type, lineLoc, first.id, matchedTokens)
                        val index = this.indexOf(first)
                        matchedTokens.forEach {
                            this.remove(it)
                        }
                        this.add(index, expression)
                    } else {
                        nativeError("Found Empty Expression!")
                        return
                    }
                }
            }
            if (!foundExpression) break
        }*/
    }

    enum class ProcessState(val displayName: String) {
        TOKENIZE("tokenizing"),
        PARSE("parsing"),
        ASSEMBLE("assembling"),
        CACHE_RESULTS("caching"),
    }

    data class Process(
        val file: CompilerFile,
        val processStart: Instant = Clock.System.now()
    ) {
        var state: ProcessState = ProcessState.TOKENIZE
            set(value) {
                field = value
                currentStateStart = Clock.System.now()
            }
        var currentStateStart: Instant = Clock.System.now()

        override fun toString(): String {
            return "${file.name} (${state.displayName} ${(Clock.System.now() - currentStateStart).inWholeSeconds} s) ${(Clock.System.now() - processStart).inWholeSeconds} s"
        }
    }

    sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {
        var hlContent = content
        private var severity: Severity? = null
        private var codeStyle: CodeStyle? = CodeStyle.BASE0

        fun isPseudo(): Boolean {
            return id < 0
        }

        fun addSeverity(severity: Severity) {
            this.severity = severity
        }

        open fun hl(vararg codeStyle: CodeStyle) {
            this.codeStyle = codeStyle.firstOrNull()
        }

        fun getCodeStyle() = codeStyle
        fun getSeverity() = severity

        override fun toString(): String = content

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

        class COMMENT(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {

            enum class CommentType(val regex: Regex) {
                SINGLELINE(Regex("^//.*\n")),
                MULTILINE(Regex("""^/\*([^*]|\*+[^*/])*\*/"""))
            }
        }

        class SYMBOL(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            companion object {
                val REGEX = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
            }
        }

        sealed class CONSTANT(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {

            sealed class NUMBER(lineLoc: LineLoc, content: String, id: Int) : CONSTANT(lineLoc, content, id) {
                class INTEGER(lineLoc: LineLoc, content: String, id: Int) : NUMBER(lineLoc, content, id) {


                    enum class IntegerFormat(val regex: Regex) {
                        BIN(Regex("^${Regex.escape(Settings.PRESTRING_BINARY)}([01]+)", RegexOption.IGNORE_CASE)),
                        OCT(Regex("^${Regex.escape(Settings.PRESTRING_BINARY)}([0-7]+)")),


                    }
                }
            }

            sealed class CHARACTER(lineLoc: LineLoc, content: String, id: Int) : CONSTANT(lineLoc, content, id) {
                sealed class STRING(lineLoc: LineLoc, content: String, id: Int) : CHARACTER(lineLoc, content, id) {

                    enum class StringType(val regex: Regex) {
                        SINGLELINE(Regex("""^"(\\.|[^\\"])*"""")),
                        MULTILINE(Regex("""^\"\"\"(?:\\.|[^\\"])*\"\"\""""))
                    }
                }

                class CHAR(lineLoc: LineLoc, content: String, id: Int) : CHARACTER(lineLoc, content, id) {
                    companion object {
                        val REGEX = Regex("""'(\\.|[^\\'])'""")
                    }
                }
            }
        }
    }

    enum class CodeStyle(val lightHexColor: Int, val darkHexColor: Int? = null) {
        RED(0xc94922),
        ORANGE(0xc76b29),
        YELLOW(0xc08b30),
        GREEN(0xac9739),
        GREENPC(0x008b19),
        CYAN(0x22a2c9),
        BLUE(0x3d8fd1),
        VIOLET(0x6679cc),
        MAGENTA(0x9c637a),
        BASE0(0x202746, 0xf5f7ff),
        BASE1(0x293256, 0xdfe2f1),
        BASE2(0x5e6687, 0x979db4),
        BASE3(0x6b7394, 0x898ea4),
        BASE4(0x898ea4, 0x6b7394),
        BASE5(0x979db4, 0x5e6687),
        BASE6(0xdfe2f1, 0x293256),
        BASE7(0xf5f7ff, 0x202746);

        fun getDarkElseLight(): Int = if (darkHexColor != null) darkHexColor else lightHexColor
    }

    data class Severity(val type: SeverityType, val message: String)

    enum class SeverityType(val codeStyle: CodeStyle) {
        ERROR(CodeStyle.RED),
        WARNING(CodeStyle.YELLOW),
        INFO(CodeStyle.BASE4)
    }

    data class ConstantPrefixes(
        val hex: String = "0x",
        val bin: String = "0b",
        val dec: String = "",
        val udec: String = "u"
    )

    data class CompileResult(val success: Boolean, val tokens: List<Token>, val tree: Syntax.SyntaxTree?)

    data class LineLoc(val fileName: String, var lineID: Int, val startIndex: Int, val endIndex: Int)

    data class CompilerFile(val name: String, val content: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CompilerFile) return false

            if (name != other.name) return false
            if (content != other.content) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + content.hashCode()
            return result
        }
    }
}