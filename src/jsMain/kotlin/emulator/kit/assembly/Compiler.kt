package emulator.kit.assembly

import emulator.kit.Settings
import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import emulator.kit.common.RegContainer
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.types.HTMLTools
import kotlin.time.measureTime

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
class Compiler(
    private val architecture: Architecture,
    private val syntax: Syntax,
    private val assembly: Assembly,
    private val prefixes: ConstantPrefixes,
    private val detectRegisters: Boolean,
    private val hlFlagCollection: HLFlagCollection
) {

    private val regexCollection: RegexCollection = RegexCollection(
        Regex("""^\s+"""),
        Regex("""^[^0-9A-Za-z]"""),
        Regex("^(-)?${Regex.escape(prefixes.bin)}[01]+"),
        Regex("^(-)?${Regex.escape(prefixes.hex)}[0-9a-f]+", RegexOption.IGNORE_CASE),
        Regex("^(-)?${Regex.escape(prefixes.dec)}[0-9]+"),
        Regex("^${Regex.escape(prefixes.udec)}[0-9]+"),
        Regex("""^'.'"""),
        Regex("""^".+""""),
        Regex("""^[a-z_][a-z0-9_]*""", RegexOption.IGNORE_CASE),
    )

    // TEMPORARY CONTENT
    private var tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private var dryLines: List<String>? = null
    private var hlLines: MutableList<String>? = null
    private var dryContent = ""
    private var syntaxTree: Syntax.SyntaxTree? = null
    private var isBuildable = false
    private var assemblyMap: Assembly.AssemblyMap = Assembly.AssemblyMap()

    /**
     * Executes and controls the compilation
     */
    fun compile(code: String, shouldHighlight: Boolean, build: Boolean = true): Boolean {
        initCode(code)

        architecture.getConsole().clear()
        val parseTime = measureTime {
            tokenize()
            parse()
        }
        architecture.getConsole().compilerInfo("build    \ttook ${parseTime.inWholeMicroseconds}µs\t(${if (isBuildable) "success" else "has errors"})")

        if (shouldHighlight) {
            val hlTime = measureTime {
                highlight()
            }
            architecture.getConsole().compilerInfo("highlight\ttook ${hlTime.inWholeMicroseconds}µs")
        }

        if (build) {
            assemble()
        }

        return isBuildable
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
    fun reassemble() {
        assemble()
    }

    /**
     * Resets all local code states
     *
     * [dryContent]
     * [dryLines]
     * [tokenList]
     * [tokenLines]
     * [hlLines]
     */
    private fun initCode(code: String) {
        tokenList = mutableListOf()
        tokenLines = mutableListOf()
        dryContent = code
        dryLines = dryContent.split(*Settings.LINEBREAKS.toTypedArray())
        hlLines = dryLines?.toMutableList()
    }

    /**
     * Transforms the [dryContent] into a List of Compiler Tokens ([tokenList], [tokenLines])
     */
    private fun tokenize() {
        dryLines?.let {
            val file = architecture.getFileHandler().getCurrent()
            for (lineID in it.indices) {
                val line = it[lineID]
                val tempTokenList = mutableListOf<Token>()
                var remainingLine = line
                var startIndex = 0

                while (remainingLine.isNotEmpty()) {
                    val space = regexCollection.space.find(remainingLine)
                    if (space != null) {
                        val token = Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += space.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val binary = regexCollection.binary.find(remainingLine)
                    if (binary != null) {
                        val token = Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), prefixes.bin, binary.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += binary.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val hex = regexCollection.hex.find(remainingLine)
                    if (hex != null) {
                        val token = Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), prefixes.hex, hex.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += hex.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val dec = regexCollection.dec.find(remainingLine)
                    if (dec != null) {
                        val token = Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), prefixes.dec, dec.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += dec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val udec = regexCollection.udec.find(remainingLine)
                    if (udec != null) {
                        val token = Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), prefixes.udec, udec.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += udec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val ascii = regexCollection.ascii.find(remainingLine)
                    if (ascii != null) {
                        val token = Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += ascii.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val string = regexCollection.string.find(remainingLine)
                    if (string != null) {
                        val token = Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += string.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val symbol = regexCollection.symbol.find(remainingLine)
                    if (symbol != null) {
                        val token = Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += symbol.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    if (detectRegisters) {
                        val regRes = regexCollection.word.find(remainingLine)
                        if (regRes != null) {
                            val reg = architecture.getRegContainer().getAllRegs(architecture.getAllFeatures()).firstOrNull { reg -> reg.names.contains(regRes.value) || reg.aliases.contains(regRes.value) }
                            if (reg != null) {
                                val token = Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                                tokenList += token
                                tempTokenList += token
                                startIndex += regRes.value.length
                                remainingLine = line.substring(startIndex)
                                continue
                            }
                        }
                    }

                    // apply rest
                    val word = regexCollection.word.find(remainingLine)

                    if (word != null) {
                        val token = Token.Word(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
                        tokenList += token
                        tempTokenList += token
                        startIndex += word.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    architecture.getConsole().warn("Assembly: no match found for $remainingLine")
                    break
                }
                val newLineToken = Token.NewLine(LineLoc(file, lineID, line.length, line.length + 2), "\n", tokenList.size)
                tokenLines.add(lineID, tempTokenList)
                tokenList += newLineToken
            }
        }
    }

    /**
     * Calls the specific [Syntax] check function which builds the [Syntax.SyntaxTree]
     */
    private fun parse() {
        architecture.getTranscript().clear()
        syntax.clear()
        syntaxTree = syntax.check(architecture, this, tokenList, tokenLines, architecture.getFileHandler().getAllFiles().filter { it != architecture.getFileHandler().getCurrent() }, architecture.getTranscript())

        syntaxTree?.rootNode?.allWarnings?.let {
            for (warning in it) {
                if (warning.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if (warning.linkedTreeNode.getAllTokens().first().isPseudo()) {
                        architecture.getConsole().warn("pseudo: Warning ${warning.message}")
                    } else {
                        architecture.getConsole().warn("line ${warning.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Warning ${warning.message}")
                    }
                } else {
                    architecture.getConsole().error("GlobalWarning: " + warning.message)
                }
            }
        }

        syntaxTree?.rootNode?.allErrors?.let { errors ->
            for (error in errors) {
                if (error.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if (error.linkedTreeNode.getAllTokens().first().isPseudo()) {
                        architecture.getConsole().error("pseudo: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                    } else {
                        architecture.getConsole().error("line ${error.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                    }
                } else {
                    architecture.getConsole().error("GlobalError: " + error.message)
                }
            }
            isBuildable = errors.isEmpty()
        }
    }

    /**
     * Builds [hlLines] from the [Compiler] tokens which highlight flags are resolved from [Syntax.ConnectedHL] in [Syntax.SyntaxTree]
     */
    private fun highlight() {

        for (lineID in tokenLines.indices) {
            val tokenLine = tokenLines[lineID]
            var hlLine = ""

            for (token in tokenLine) {
                if (syntaxTree?.rootNode != null) {
                    val node = syntaxTree?.contains(token)?.elementNode
                    if (node != null) {
                        val hlFlag = node.highlighting.getHLFlag(token)
                        if (hlFlag != null) {
                            token.hl(architecture, hlFlag, node.name)
                            hlLine += token.hlContent
                            continue
                        }
                    }
                    if (syntaxTree?.errorsContain(token) == true) {
                        token.hl(architecture, hlFlagCollection.error ?: "", "error")
                        hlLine += token.hlContent
                        continue
                    }
                }

                if (syntax.applyStandardHLForRest) {
                    when (token) {
                        is Token.Constant.Binary -> {
                            hlFlagCollection.constBin?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Dec -> {
                            hlFlagCollection.constDec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Hex -> {
                            hlFlagCollection.constHex?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.UDec -> {
                            hlFlagCollection.constUDec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Ascii -> {
                            hlFlagCollection.constAscii?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.String -> {
                            hlFlagCollection.constAscii?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Register -> {
                            hlFlagCollection.register?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Symbol -> {
                            hlFlagCollection.symbol?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Word -> {
                            hlFlagCollection.word?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Space -> {
                            hlFlagCollection.whitespace?.let {
                                token.hl(architecture, it)
                            }
                        }

                        else -> {

                        }
                    }
                } else {
                    token.hl(architecture, "")
                }

                hlLine += token.hlContent
            }

            hlLines?.let {
                it[lineID] = hlLine
            }
        }
    }

    /**
     * Calls the specific [Assembly.assemble] function which analyzes the [Syntax.SyntaxTree] and stores the resulting bytes into the Memory
     */
    private fun assemble() {
        architecture.getMemory().clear()
        architecture.getRegContainer().pc.reset()
        architecture.getTranscript().clear(Transcript.Type.DISASSEMBLED)

        if (isBuildable) {
            syntaxTree?.let {
                val assembleTime = measureTime {
                    assemblyMap = assembly.assemble(architecture, it)
                }
                architecture.getConsole().compilerInfo("assembl\ttook ${assembleTime.inWholeMicroseconds}µs")


                val disassembleTime = measureTime {
                    assembly.disassemble(architecture)
                }
                architecture.getConsole().compilerInfo("disassembl\ttook ${disassembleTime.inWholeMicroseconds}µs")

                architecture.getFileHandler().getCurrent().linkGrammarTree(it)
            }
        } else {
            syntaxTree?.let {
                architecture.getFileHandler().getCurrent().linkGrammarTree(it)
            }
        }
    }

    /**
     * This function could be used to insert and [pseudoTokenize] custom inserted code such as macro inserts to get its resolving [Compiler.Token]'s from it.
     * This should only be called while building the [Syntax.SyntaxTree]!
     */
    fun pseudoTokenize(content: String, lineID: Int = Settings.COMPILER_TOKEN_PSEUDOID): List<Token> {
        val tokens = mutableListOf<Token>()
        var remaining = content
        var startIndex = 0
        val file = architecture.getFileHandler().getCurrent()
        while (remaining.isNotEmpty()) {
            val space = regexCollection.space.find(remaining)
            if (space != null) {
                tokens += Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, lineID)
                startIndex += space.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val binary = regexCollection.binary.find(remaining)
            if (binary != null) {
                tokens += Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), prefixes.bin, binary.value, lineID)
                startIndex += binary.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                tokens += Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), prefixes.hex, hex.value, lineID)
                startIndex += hex.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                tokens += Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), prefixes.dec, dec.value, lineID)
                startIndex += dec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                tokens += Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), prefixes.udec, udec.value, lineID)
                startIndex += udec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                tokens += Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, lineID)
                startIndex += ascii.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                tokens += Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, lineID)
                startIndex += string.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val symbol = regexCollection.symbol.find(remaining)
            if (symbol != null) {
                tokens += Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, lineID)
                startIndex += symbol.value.length
                remaining = content.substring(startIndex)
                continue
            }

            if (detectRegisters) {
                val regRes = regexCollection.word.find(remaining)
                if (regRes != null) {
                    val reg = architecture.getRegContainer().getReg(regRes.value, architecture.getAllFeatures())
                    if (reg != null) {
                        tokens += Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, lineID)
                        startIndex += regRes.value.length
                        remaining = content.substring(startIndex)
                        continue

                    }
                }
            }

            // apply rest
            val word = regexCollection.word.find(remaining)
            if (word != null) {
                tokens += Token.Word(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, lineID)
                startIndex += word.value.length
                remaining = content.substring(startIndex)
                continue
            }

            architecture.getConsole().warn("Assembly.analyze($content): no match found for $remaining")
            break
        }

        return tokens
    }

    /**
     * returns the highlighted code
     */
    fun getHLContent(): String {
        val stringBuilder = StringBuilder()
        hlLines?.let {
            for (line in it) {
                stringBuilder.append("$line\n")
            }
        }
        val hlContent = stringBuilder.toString()
        return hlContent.ifEmpty {
            dryContent
        }
    }

    /**
     * returns the current [Syntax.SyntaxTree]
     */
    fun getGrammarTree(): Syntax.SyntaxTree? = syntaxTree

    sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {

        var hlContent = content
        abstract val type: TokenType

        fun isPseudo(): Boolean {
            return id == Settings.COMPILER_TOKEN_PSEUDOID && lineLoc.lineID == Settings.COMPILER_TOKEN_PSEUDOID
        }

        fun hl(architecture: Architecture, hlFlag: String, title: String = "") {
            hlContent = architecture.highlight(HTMLTools.encodeHTML(content), id, title, hlFlag, "token")
        }

        override fun toString(): String = content

        class NewLine(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.NEWLINE
        }

        class Space(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SPACE
        }

        class Symbol(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SYMBOL
        }

        sealed class Constant(lineLoc: LineLoc, content: kotlin.String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.CONSTANT
            abstract fun getValue(size: Variable.Size? = null): Variable.Value

            class Ascii(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    var binChars = ""
                    val byteArray = content.substring(1, content.length - 1).encodeToByteArray()
                    for (byte in byteArray) {
                        val bin = byte.toInt().toString(2)
                        binChars += bin
                    }
                    return if (size != null) {
                        Variable.Value.Bin(binChars, size)
                    } else {
                        Variable.Value.Bin(binChars)
                    }
                }
            }

            class String(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    var hexStr = ""
                    val trimmedContent = content.substring(1, content.length - 1)
                    for (char in trimmedContent) {
                        val hexChar = char.code.toString(16)
                        hexStr += hexChar
                    }
                    return if (size != null) {
                        Variable.Value.Hex(hexStr, size)
                    } else {
                        Variable.Value.Hex(hexStr)
                    }
                }
            }

            class Binary(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return if (size != null) {
                        if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefix), size) else Variable.Value.Bin(content.removePrefix(prefix), size)
                    } else {
                        if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefix)) else Variable.Value.Bin(content.removePrefix(prefix))
                    }
                }
            }

            class Hex(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return if (size != null) {
                        if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefix), size) else Variable.Value.Hex(content.removePrefix(prefix), size)
                    } else {
                        if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefix)) else Variable.Value.Hex(content.removePrefix(prefix))
                    }
                }
            }

            class Dec(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return if (size != null) {
                        Variable.Value.Dec(content.removePrefix(prefix), size)
                    } else {
                        Variable.Value.Dec(content.removePrefix(prefix))
                    }
                }
            }

            class UDec(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return if (size != null) {
                        Variable.Value.UDec(content.removePrefix(prefix), size)
                    } else {
                        Variable.Value.UDec(content.removePrefix(prefix))
                    }
                }
            }

        }

        class Register(lineLoc: LineLoc, content: String, val reg: RegContainer.Register, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.REGISTER
        }

        class Word(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.WORD
        }
    }

    enum class TokenType {
        SPACE,
        NEWLINE,
        SYMBOL,
        CONSTANT,
        REGISTER,
        WORD
    }

    data class HLFlagCollection(
        val alphaNum: String? = null,
        val word: String? = null,
        val constHex: String? = null,
        val constBin: String? = null,
        val constDec: String? = null,
        val constUDec: String? = null,
        val constAscii: String? = null,
        val constString: String? = null,
        val register: String? = null,
        val symbol: String? = null,
        val instruction: String? = null,
        val comment: String? = null,
        val whitespace: String? = null,
        val error: String? = null
    )

    data class RegexCollection(
        val space: Regex,
        val symbol: Regex,
        val binary: Regex,
        val hex: Regex,
        val dec: Regex,
        val udec: Regex,
        val ascii: Regex,
        val string: Regex,
        val word: Regex,
    )

    data class ConstantPrefixes(
        val hex: String = "0x",
        val bin: String = "0b",
        val dec: String = "",
        val udec: String = "u"
    )

    data class LineLoc(val file: FileHandler.File, var lineID: Int, val startIndex: Int, val endIndex: Int)
    // endIndex means index after last Character
}