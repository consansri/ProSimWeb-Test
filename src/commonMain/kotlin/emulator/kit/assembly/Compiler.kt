package emulator.kit.assembly

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
import io.nacular.doodle.controls.form.file

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
    private val detectRegisters: Boolean
) {

    private val regexCollection: RegexCollection = RegexCollection(
        Regex("""^[\t ]+"""),
        Regex("^(\\r\\n|\\r|\\n)"),
        Regex("""^[^0-9A-Za-z]"""),
        Regex("^(-)?${Regex.escape(prefixes.bin)}[01]+"),
        Regex("^(-)?${Regex.escape(prefixes.hex)}[0-9a-f]+", RegexOption.IGNORE_CASE),
        Regex("^(-)?${Regex.escape(prefixes.dec)}[0-9]+"),
        Regex("^${Regex.escape(prefixes.udec)}[0-9]+"),
        Regex("""^'.'"""),
        Regex("""^".+""""),
        Regex("""^[a-z]+""", RegexOption.IGNORE_CASE),
        Regex("""^[a-z][a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""^[a-z_][a-z0-9_]+""", RegexOption.IGNORE_CASE),
        Regex("""^[.a-z_][.a-z0-9_]+""", RegexOption.IGNORE_CASE)
    )

    // TEMPORARY CONTENT
    private var tokenList: MutableList<Token> = mutableListOf()
    private var dryContent = ""
    private var syntaxTree: Syntax.SyntaxTree? = null
    private var isBuildable = false
    private var assemblyMap: Assembly.AssemblyMap = Assembly.AssemblyMap()
    private var pseudoID = -1

    /**
     * Executes and controls the compilation
     */
    fun compile(code: String, filename: String, others: List<Syntax.LinkedTree>, build: Boolean = true): CompileResult {
        initCode(code)

        architecture.getConsole().clear()
        val parseTime = measureTime {
            tokenize(filename)
            parse(others)
        }
        architecture.getConsole().compilerInfo("build    \ttook ${parseTime.inWholeMicroseconds}µs\t(${if (isBuildable) "success" else "has errors"})")

        if (build) {
            assemble()
        }

        return CompileResult(isBuildable, tokenList, syntaxTree)
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
     * [hlContent]
     */
    private fun initCode(code: String) {
        tokenList = mutableListOf()
        dryContent = code
        pseudoID = -1
    }

    /**
     * Transforms the [dryContent] into a List of Compiler Tokens ([tokenList], [tokenLines])
     */
    private fun tokenize(fileName: String) {
        var remaining = dryContent
        var lineID = 0
        var startIndex = 0

        while (remaining.isNotEmpty()) {
            val newLine = regexCollection.newLine.find(remaining)
            if (newLine != null) {
                val token = Token.NewLine(LineLoc(fileName, lineID, startIndex, startIndex + newLine.value.length), newLine.value, tokenList.size)
                tokenList += token
                startIndex += newLine.value.length
                remaining = dryContent.substring(startIndex)
                lineID += 1
                continue
            }

            val space = regexCollection.space.find(remaining)
            if (space != null) {
                val token = Token.Space(LineLoc(fileName, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                tokenList += token
                startIndex += space.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val binary = regexCollection.bin.find(remaining)
            if (binary != null) {
                val token = Token.Constant.Binary(LineLoc(fileName, lineID, startIndex, startIndex + binary.value.length), prefixes.bin, binary.value, tokenList.size)
                tokenList += token
                startIndex += binary.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                val token = Token.Constant.Hex(LineLoc(fileName, lineID, startIndex, startIndex + hex.value.length), prefixes.hex, hex.value, tokenList.size)
                tokenList += token
                startIndex += hex.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                val token = Token.Constant.Dec(LineLoc(fileName, lineID, startIndex, startIndex + dec.value.length), prefixes.dec, dec.value, tokenList.size)
                tokenList += token
                startIndex += dec.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                val token = Token.Constant.UDec(LineLoc(fileName, lineID, startIndex, startIndex + udec.value.length), prefixes.udec, udec.value, tokenList.size)
                tokenList += token
                startIndex += udec.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                val token = Token.Constant.Ascii(LineLoc(fileName, lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                tokenList += token
                startIndex += ascii.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                val token = Token.Constant.String(LineLoc(fileName, lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                tokenList += token
                startIndex += string.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            if (detectRegisters) {
                val regRes = regexCollection.wordNum.find(remaining)
                if (regRes != null) {
                    val reg = architecture.getRegContainer().getAllRegs(architecture.getAllFeatures()).firstOrNull { reg -> reg.names.contains(regRes.value) || reg.aliases.contains(regRes.value) }
                    if (reg != null) {
                        val token = Token.Register(LineLoc(fileName, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                        tokenList += token
                        startIndex += regRes.value.length
                        remaining = dryContent.substring(startIndex)
                        continue
                    }
                }
            }

            val word = regexCollection.word.find(remaining)
            val wordNum = regexCollection.wordNum.find(remaining)
            val wordNumUs = regexCollection.wordNumUs.find(remaining)
            val wordNumUsDot = regexCollection.wordNumUsDots.find(remaining)

            var wordToken: Token.Word? = null
            if (word != null) {
                wordToken = Token.Word.Clean(LineLoc(fileName, lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
            }

            if (wordNum != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.Num(LineLoc(fileName, lineID, startIndex, startIndex + wordNum.value.length), wordNum.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNum.value.length) {
                        wordToken = Token.Word.Num(LineLoc(fileName, lineID, startIndex, startIndex + wordNum.value.length), wordNum.value, tokenList.size)
                    }
                }
            }

            if (wordNumUs != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumUs(LineLoc(fileName, lineID, startIndex, startIndex + wordNumUs.value.length), wordNumUs.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNumUs.value.length) {
                        wordToken = Token.Word.NumUs(LineLoc(fileName, lineID, startIndex, startIndex + wordNumUs.value.length), wordNumUs.value, tokenList.size)
                    }
                }
            }

            if (wordNumUsDot != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumDotsUs(LineLoc(fileName, lineID, startIndex, startIndex + wordNumUsDot.value.length), wordNumUsDot.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNumUsDot.value.length) {
                        wordToken = Token.Word.NumDotsUs(LineLoc(fileName, lineID, startIndex, startIndex + wordNumUsDot.value.length), wordNumUsDot.value, tokenList.size)
                    }
                }
            }

            // Add word if found
            if (wordToken != null) {
                tokenList += wordToken
                startIndex += wordToken.content.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val symbol = regexCollection.symbol.find(remaining)
            if (symbol != null) {
                val token = Token.Symbol(LineLoc(fileName, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                tokenList += token
                startIndex += symbol.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            architecture.getConsole().warn("Assembly: no match found for $remaining")
            break
        }

        tokenList.resolveExpressions()

    }

    /**
     * Calls the specific [Syntax] check function which builds the [Syntax.SyntaxTree]
     */
    private fun parse(others: List<Syntax.LinkedTree>) {
        syntax.clear()
        syntaxTree = syntax.check(
            architecture,
            this,
            tokenList,
            others,
            architecture.getTranscript()
        )

        syntaxTree?.rootNode?.allWarnings?.let {
            for (warning in it) {
                warning.linkedTreeNode.getAllTokens().forEach { it.addSeverity(Severity(SeverityType.WARNING, warning.message)) }
                if (warning.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    architecture.getConsole().warn("line ${warning.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Warning ${warning.message}")
                } else {
                    architecture.getConsole().error("GlobalWarning: " + warning.message)
                }
            }
        }

        syntaxTree?.rootNode?.allErrors?.let { errors ->
            for (error in errors) {
                error.linkedTreeNode.getAllTokens().forEach { it.addSeverity(Severity(SeverityType.ERROR, error.message)) }
                if (error.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    architecture.getConsole().error("line ${error.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                } else {
                    architecture.getConsole().error("GlobalError: " + error.message)
                }
            }
            isBuildable = errors.isEmpty()
        }
    }

    /**
     * Calls the specific [Assembly.assemble] function which analyzes the [Syntax.SyntaxTree] and stores the resulting bytes into the Memory
     */
    private fun assemble() {
        if (isBuildable) {
            architecture.getMemory().clear()
            architecture.getRegContainer().pc.reset()
            architecture.getTranscript().clear()

            syntaxTree?.let {
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

            val newLine = regexCollection.newLine.find(remaining)
            if (newLine != null) {
                pseudoTokens += Token.NewLine(lineLoc, newLine.value, pseudoID)
                startIndex += newLine.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val space = regexCollection.space.find(remaining)
            if (space != null) {
                pseudoTokens += Token.Space(lineLoc, space.value, pseudoID)
                startIndex += space.value.length
                remaining = content.substring(startIndex)
                continue
            }

            /*var foundCalcToken = false
            for (mode in Token.Constant.Calculated.MODE.entries) {
                val result = mode.regex.find(remaining) ?: continue
                val token = mode.getCalcToken(lineLoc, prefixes, result.groups, result.value, pseudoID, regexCollection) ?: continue
                pseudoTokens += token
                foundCalcToken = true
                startIndex += result.value.length
                remaining = content.substring(startIndex)
                break
            }
            if (foundCalcToken) continue*/

            val binary = regexCollection.bin.find(remaining)
            if (binary != null) {
                pseudoTokens += Token.Constant.Binary(lineLoc, prefixes.bin, binary.value, pseudoID)
                startIndex += binary.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                pseudoTokens += Token.Constant.Hex(lineLoc, prefixes.hex, hex.value, pseudoID)
                startIndex += hex.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                pseudoTokens += Token.Constant.Dec(lineLoc, prefixes.dec, dec.value, pseudoID)
                startIndex += dec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                pseudoTokens += Token.Constant.UDec(lineLoc, prefixes.udec, udec.value, pseudoID)
                startIndex += udec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                pseudoTokens += Token.Constant.Ascii(lineLoc, ascii.value, pseudoID)
                startIndex += ascii.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                pseudoTokens += Token.Constant.String(lineLoc, string.value, pseudoID)
                startIndex += string.value.length
                remaining = content.substring(startIndex)
                continue
            }

            if (detectRegisters) {
                val regRes = regexCollection.wordNum.find(remaining)
                if (regRes != null) {
                    val reg = architecture.getRegContainer().getReg(regRes.value, architecture.getAllFeatures())
                    if (reg != null) {
                        pseudoTokens += Token.Register(lineLoc, regRes.value, reg, pseudoID)
                        startIndex += regRes.value.length
                        remaining = content.substring(startIndex)
                        continue

                    }
                }
            }

            val word = regexCollection.word.find(remaining)
            val wordNum = regexCollection.wordNum.find(remaining)
            val wordNumUs = regexCollection.wordNumUs.find(remaining)
            val wordNumUsDots = regexCollection.wordNumUsDots.find(remaining)

            var wordToken: Token.Word? = null
            if (word != null) {
                wordToken = Token.Word.Clean(lineLoc, word.value, pseudoID)
            }

            if (wordNum != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.Num(lineLoc, wordNum.value, pseudoID)
                } else {
                    if (wordToken.content.length < wordNum.value.length) {
                        wordToken = Token.Word.Num(lineLoc, wordNum.value, pseudoID)
                    }
                }
            }

            if (wordNumUs != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumUs(lineLoc, wordNumUs.value, pseudoID)
                } else {
                    if (wordToken.content.length < wordNumUs.value.length) {
                        wordToken = Token.Word.NumUs(lineLoc, wordNumUs.value, pseudoID)
                    }
                }
            }

            if (wordNumUsDots != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumDotsUs(lineLoc, wordNumUsDots.value, pseudoID)
                } else {
                    if (wordToken.content.length < wordNumUsDots.value.length) {
                        wordToken = Token.Word.NumDotsUs(lineLoc, wordNumUsDots.value, pseudoID)
                    }
                }
            }

            if (wordToken != null) {
                pseudoTokens += wordToken
                startIndex += wordToken.content.length
                remaining = content.substring(startIndex)
                continue
            }

            val symbol = regexCollection.symbol.find(remaining)
            if (symbol != null) {
                pseudoTokens += Token.Symbol(lineLoc, symbol.value, pseudoID)
                startIndex += symbol.value.length
                remaining = content.substring(startIndex)
                continue
            }

            architecture.getConsole().warn("Assembly.analyze($content): no match found for $remaining")
            break
        }

        pseudoTokens.resolveExpressions()

        return pseudoTokens
    }

    /**
     * returns the highlighted code
     */
    fun getHLContent(): List<Token> {
        return tokenList
    }

    /**
     * returns the current [Syntax.SyntaxTree]
     */
    fun getGrammarTree(): Syntax.SyntaxTree? = syntaxTree

    private fun MutableList<Token>.resolveExpressions() {
        while (true) {
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

        class NewLine(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id)

        class Space(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id)

        class Symbol(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id)

        sealed class Constant(lineLoc: LineLoc, content: kotlin.String, id: Int) : Token(lineLoc, content, id) {
            abstract fun getValue(size: Variable.Size? = null, onlyUnsigned: Boolean = false): Variable.Value

            class Ascii(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
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
                val rawString = content.substring(1, content.length - 1)

                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    var hexStr = ""
                    val trimmedContent = rawString
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

            class Expression(val type: ExpressionType, lineLoc: LineLoc, id: Int, val tokens: List<Token>) : Constant(lineLoc, tokens.joinToString("") { it.content }, id) {

                val constants: List<Constant> = tokens.filterIsInstance<Constant>()

                /**
                 * Function for Highlighting Expressions
                 *
                 * [codeStyle.size] sets highlighting to one of the following modes
                 *
                 * 0 Codestyles (no highlighting will be applied)
                 *
                 * 1 CodeStyle (leaving brackets and operators blank)
                 *  1. for [Token.Constant]
                 *
                 * 2 CodeStyles (leaving brackets blank)
                 *  1. for [Token.Constant]
                 *  2. for Operators
                 *
                 * 3 or more CodeStyles
                 *  1. for [Token.Constant]
                 *  2. for Operators
                 *  3. for Brackets
                 *
                 */
                override fun hl(vararg codeStyle: CodeStyle) {

                    when (codeStyle.size) {
                        0 -> {}
                        1 -> {
                            tokens.filterIsInstance<Constant>().forEach { it.hl(*codeStyle) }
                        }

                        2 -> {
                            tokens.filterIsInstance<Constant>().forEach { it.hl(*codeStyle) }
                            tokens.filterIsInstance<Symbol>().forEach {
                                when (it.content) {
                                    "(" -> {
                                        nativeLog("Found Bracket!")
                                        it.hl()
                                    }

                                    ")" -> {
                                        it.hl()
                                    }

                                    else -> {
                                        it.hl(codeStyle.get(1))
                                    }
                                }
                            }
                        }

                        else -> {
                            tokens.filterIsInstance<Constant>().forEach { it.hl(*codeStyle) }
                            tokens.filterIsInstance<Symbol>().forEach {
                                when (it.content) {
                                    "(" -> {
                                        it.hl(codeStyle.get(2))
                                    }

                                    ")" -> {
                                        it.hl(codeStyle.get(2))
                                    }

                                    else -> {
                                        it.hl(codeStyle.get(1))
                                    }
                                }
                            }
                        }
                    }
                }

                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    return when (type) {
                        ExpressionType.BRACKETS -> constants[0].getValue(size, onlyUnsigned)
                        ExpressionType.ADD -> (constants[0].getValue(size, onlyUnsigned) + constants[1].getValue(size, onlyUnsigned))
                        ExpressionType.SUB -> (constants[0].getValue(size, onlyUnsigned) - constants[1].getValue(size, onlyUnsigned))
                        ExpressionType.MUL -> (constants[0].getValue(size, onlyUnsigned) * constants[1].getValue(size, onlyUnsigned))
                        ExpressionType.DIV -> (constants[0].getValue(size, onlyUnsigned) / constants[1].getValue(size, onlyUnsigned))
                        ExpressionType.SHL -> {
                            val shiftAmount = constants[1].getValue(Bit16(), true).toUDec().toIntOrNull() ?: 0
                            constants[0].getValue(size, onlyUnsigned).toBin().shl(shiftAmount)
                        }

                        ExpressionType.SHR -> {
                            val shiftAmount = constants[1].getValue(Bit16(), true).toUDec().toIntOrNull() ?: 0
                            constants[0].getValue(size, onlyUnsigned).toBin().shl(shiftAmount)
                        }
                    }
                }

                enum class ExpressionType(val tokenSeq: TokenSeq) {
                    BRACKETS(TokenSeq(Specific("("), Constant, Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    ADD(TokenSeq(Specific("("), Constant, Specific("+"), Constant, Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    SUB(TokenSeq(Specific("("), Constant, Specific("-"), Constant, Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    MUL(TokenSeq(Specific("("), Constant, Specific("*"), Constant, Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    DIV(TokenSeq(Specific("("), Constant, Specific("/"), Constant, Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    SHL(TokenSeq(Specific("("), Constant, Specific("<"), Specific("<"), SpecConst(Bit16(), onlyUnsigned = true), Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true)),
                    SHR(TokenSeq(Specific("("), Constant, Specific(">"), Specific(">"), SpecConst(Bit16(), onlyUnsigned = true), Specific(")"), ignoreSpaces = true, addIgnoredSpacesToMap = true))
                }
            }

            class Binary(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    return if (size != null) {
                        if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefix), size) else Variable.Value.Bin(content.removePrefix(prefix), size)
                    } else {
                        if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefix)) else Variable.Value.Bin(content.removePrefix(prefix))
                    }
                }
            }

            class Hex(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    return if (size != null) {
                        if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefix), size) else Variable.Value.Hex(content.removePrefix(prefix), size)
                    } else {
                        if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefix)) else Variable.Value.Hex(content.removePrefix(prefix))
                    }
                }
            }

            class Dec(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    return if (size != null) {
                        if (onlyUnsigned) Variable.Value.UDec(content.removePrefix(prefix), size) else Variable.Value.Dec(content.removePrefix(prefix), size)
                    } else {
                        if (onlyUnsigned) Variable.Value.UDec(content.removePrefix(prefix)) else Variable.Value.Dec(content.removePrefix(prefix))
                    }
                }
            }

            class UDec(lineLoc: LineLoc, private val prefix: kotlin.String, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    return if (size != null) {
                        Variable.Value.UDec(content.removePrefix(prefix), size)
                    } else {
                        Variable.Value.UDec(content.removePrefix(prefix))
                    }
                }
            }

        }

        class Register(lineLoc: LineLoc, content: String, val reg: RegContainer.Register, id: Int) : Token(lineLoc, content, id)

        sealed class Word(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            class NumDotsUs(lineLoc: LineLoc, content: String, id: Int) : Word(lineLoc, content, id)
            class NumUs(lineLoc: LineLoc, content: String, id: Int) : Word(lineLoc, content, id)
            class Num(lineLoc: LineLoc, content: String, id: Int) : Word(lineLoc, content, id)
            class Clean(lineLoc: LineLoc, content: String, id: Int) : Word(lineLoc, content, id)
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

    data class RegexCollection(
        val space: Regex,
        val newLine: Regex,
        val symbol: Regex,
        val bin: Regex,
        val hex: Regex,
        val dec: Regex,
        val udec: Regex,
        val ascii: Regex,
        val string: Regex,
        val word: Regex,
        val wordNum: Regex,
        val wordNumUs: Regex,
        val wordNumUsDots: Regex
    )

    data class ConstantPrefixes(
        val hex: String = "0x",
        val bin: String = "0b",
        val dec: String = "",
        val udec: String = "u"
    )

    data class CompileResult(val success: Boolean, val tokens: List<Compiler.Token>, val tree: Syntax.SyntaxTree?)

    data class LineLoc(val fileName: String, var lineID: Int, val startIndex: Int, val endIndex: Int)
// endIndex means index after last Character
}