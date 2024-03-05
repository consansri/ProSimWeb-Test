package emulator.kit.assembly

import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import emulator.kit.common.RegContainer
import emulator.kit.types.Variable
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
    fun compile(code: String, shouldHighlight: Boolean, build: Boolean = true): Boolean {
        initCode(code)

        architecture.getConsole().clear()
        val parseTime = measureTime {
            tokenize()
            parse()
        }
        architecture.getConsole().compilerInfo("build    \ttook ${parseTime.inWholeMicroseconds}µs\t(${if (isBuildable) "success" else "has errors"})")

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
    private fun tokenize() {
        val file = architecture.getFileHandler().getCurrent()
        var remaining = dryContent
        var lineID = 0
        var startIndex = 0

        while (remaining.isNotEmpty()) {
            val newLine = regexCollection.newLine.find(remaining)
            if (newLine != null) {
                val token = Token.NewLine(LineLoc(file, lineID, startIndex, startIndex + newLine.value.length), newLine.value, tokenList.size)
                tokenList += token
                startIndex += newLine.value.length
                remaining = dryContent.substring(startIndex)
                lineID += 1
                continue
            }

            val space = regexCollection.space.find(remaining)
            if (space != null) {
                val token = Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                tokenList += token
                startIndex += space.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            var foundCalcToken = false
            for (mode in Token.Constant.Calculated.MODE.entries) {
                val result = mode.regex.find(remaining) ?: continue
                val token = mode.getCalcToken(LineLoc(file, lineID, startIndex, startIndex + result.value.length), prefixes, result.groups, result.value, tokenList.size, regexCollection) ?: continue
                foundCalcToken = true
                tokenList += token
                startIndex += result.value.length
                remaining = dryContent.substring(startIndex)
                break
            }
            if (foundCalcToken) continue

            val binary = regexCollection.bin.find(remaining)
            if (binary != null) {
                val token = Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), prefixes.bin, binary.value, tokenList.size)
                tokenList += token
                startIndex += binary.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                val token = Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), prefixes.hex, hex.value, tokenList.size)
                tokenList += token
                startIndex += hex.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                val token = Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), prefixes.dec, dec.value, tokenList.size)
                tokenList += token
                startIndex += dec.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                val token = Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), prefixes.udec, udec.value, tokenList.size)
                tokenList += token
                startIndex += udec.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                val token = Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                tokenList += token
                startIndex += ascii.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                val token = Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
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
                        val token = Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
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
                wordToken = Token.Word.Clean(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
            }

            if (wordNum != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.Num(LineLoc(file, lineID, startIndex, startIndex + wordNum.value.length), wordNum.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNum.value.length) {
                        wordToken = Token.Word.Num(LineLoc(file, lineID, startIndex, startIndex + wordNum.value.length), wordNum.value, tokenList.size)
                    }
                }
            }

            if (wordNumUs != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumUs(LineLoc(file, lineID, startIndex, startIndex + wordNumUs.value.length), wordNumUs.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNumUs.value.length) {
                        wordToken = Token.Word.NumUs(LineLoc(file, lineID, startIndex, startIndex + wordNumUs.value.length), wordNumUs.value, tokenList.size)
                    }
                }
            }

            if (wordNumUsDot != null) {
                if (wordToken == null) {
                    wordToken = Token.Word.NumDotsUs(LineLoc(file, lineID, startIndex, startIndex + wordNumUsDot.value.length), wordNumUsDot.value, tokenList.size)
                } else {
                    if (wordToken.content.length < wordNumUsDot.value.length) {
                        wordToken = Token.Word.NumDotsUs(LineLoc(file, lineID, startIndex, startIndex + wordNumUsDot.value.length), wordNumUsDot.value, tokenList.size)
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
                val token = Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                tokenList += token
                startIndex += symbol.value.length
                remaining = dryContent.substring(startIndex)
                continue
            }

            architecture.getConsole().warn("Assembly: no match found for $remaining")
            break
        }
    }

    /**
     * Calls the specific [Syntax] check function which builds the [Syntax.SyntaxTree]
     */
    private fun parse() {
        syntax.clear()
        syntaxTree = syntax.check(
            architecture,
            this,
            tokenList,
            architecture.getFileHandler().getAllFiles().filter { it != architecture.getFileHandler().getCurrent() },
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
    fun pseudoTokenize(content: String, lineLoc: LineLoc): List<Token> {
        val pseudoTokens = mutableListOf<Token>()
        var remaining = content
        var startIndex = 0
        val file = architecture.getFileHandler().getCurrent()
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


            var foundCalcToken = false
            for (mode in Token.Constant.Calculated.MODE.entries) {
                val result = mode.regex.find(remaining) ?: continue
                val token = mode.getCalcToken(lineLoc, prefixes, result.groups, result.value, pseudoID, regexCollection) ?: continue
                pseudoTokens += token
                foundCalcToken = true
                startIndex += result.value.length
                remaining = content.substring(startIndex)
                break
            }
            if (foundCalcToken) continue

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

        fun hl(codeStyle: CodeStyle) {
            this.codeStyle = codeStyle
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

            class Calculated(lineLoc: LineLoc, val mode: MODE, val prefixes: ConstantPrefixes, val groupValues: MatchGroupCollection, val regexCollection: RegexCollection, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(size: Variable.Size?, onlyUnsigned: Boolean): Variable.Value {
                    val value1content = groupValues["val1"]?.value
                    val value2content = groupValues["val2"]?.value

                    if (value1content == null || value2content == null) throw Error("Missing number for calculation extract calculated number!\n$lineLoc\n$mode\n$groupValues$prefixes")

                    val value1 = getNumber(value1content, size, regexCollection, onlyUnsigned) ?: throw Error("Missing number (value1 == null) for calculation extract calculated number!\n$lineLoc\n$mode\n$groupValues$prefixes")
                    val value2 = getNumber(value2content, size, regexCollection, onlyUnsigned) ?: throw Error("Missing number (value2 == null) for calculation extract calculated number!\n$lineLoc\n$mode\n$groupValues$prefixes")



                    return when (mode) {
                        MODE.SHIFTLEFT -> {
                            val int = value2.toUDec().toIntOrNull() ?: throw Error("Dec (${value2.toDec()}) couldn't be transformed to Int!\n$lineLoc\n$mode\n$groupValues$prefixes")
                            value1.toBin() shl int
                        }

                        MODE.SHIFTRIGHT -> {
                            val int = value2.toUDec().toIntOrNull() ?: throw Error("Dec (${value2.toDec()}) couldn't be transformed to Int!\n$lineLoc\n$mode\n$groupValues$prefixes")
                            value1.toBin() shr int
                        }

                        MODE.ADD -> value1 + value2

                        MODE.SUB -> value1 - value2

                        MODE.MUL -> value1 * value2
                        MODE.DIV -> value1 / value2
                    }
                }

                private fun getNumber(content: kotlin.String, size: Variable.Size?, regexCollection: RegexCollection, onlyUnsigned: Boolean): Variable.Value? {
                    var result = regexCollection.bin.matchEntire(content)
                    if (result != null) {
                        return if (size != null) {
                            if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefixes.bin), size) else Variable.Value.Bin(content.removePrefix(prefixes.bin), size)
                        } else {
                            if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-').removePrefix(prefixes.bin)) else Variable.Value.Bin(content.removePrefix(prefixes.bin))
                        }
                    }
                    result = regexCollection.hex.matchEntire(content)
                    if (result != null) {
                        return if (size != null) {
                            if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefixes.hex), size) else Variable.Value.Hex(content.removePrefix(prefixes.hex), size)
                        } else {
                            if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-').removePrefix(prefixes.hex)) else Variable.Value.Hex(content.removePrefix(prefixes.hex))
                        }
                    }
                    result = regexCollection.udec.matchEntire(content)
                    if (result != null) {
                        return if (size != null) {
                            Variable.Value.UDec(content.removePrefix(prefixes.udec), size)
                        } else {
                            Variable.Value.UDec(content.removePrefix(prefixes.udec))
                        }
                    }
                    result = regexCollection.dec.matchEntire(content)
                    if (result != null) {
                        return if (size != null) {
                            if (onlyUnsigned) Variable.Value.UDec(content.removePrefix(prefixes.dec), size) else Variable.Value.Dec(content.removePrefix(prefixes.dec), size)
                        } else {
                            if (onlyUnsigned) Variable.Value.UDec(content.removePrefix(prefixes.dec)) else Variable.Value.Dec(content.removePrefix(prefixes.dec))
                        }
                    }
                    return null
                }

                enum class MODE(val regex: Regex) {
                    SHIFTLEFT(Regex("""^\(\s*(?<val1>\S+)\s*<<\s*(?<val2>\S+)\s*\)""")),
                    SHIFTRIGHT(Regex("""^\(\s*(?<val1>\S+)\s*>>\s*(?<val2>\S+)\s*\)""")),
                    ADD(Regex("""^\(\s*(?<val1>\S+)\s*\+\s*(?<val2>\S+)\s*\)""")),
                    SUB(Regex("""^\(\s*(?<val1>\S+)\s*-\s*(?<val2>\S+)\s*\)""")),
                    MUL(Regex("""^\(\s*(?<val1>\S+)\s*\*\s*(?<val2>\S+)\s*\)""")),
                    DIV(Regex("""^\(\s*(?<val1>\S+)\s*/\s*(?<val2>\S+)\s*\)"""));

                    fun getCalcToken(lineLoc: LineLoc, prefixes: ConstantPrefixes, groupValues: MatchGroupCollection, content: kotlin.String, id: Int, regexCollection: RegexCollection): Calculated? {
                        when (this) {
                            SHIFTLEFT, SHIFTRIGHT, ADD, SUB, MUL, DIV -> {
                                val value1 = groupValues.get("val1")?.value
                                val value2 = groupValues.get("val2")?.value

                                if (value1 == null || value2 == null) return null

                                if (checkIfNumber(value1, regexCollection) == null) return null
                                if (checkIfNumber(value2, regexCollection) == null) return null

                                return Calculated(lineLoc, this, prefixes, groupValues, regexCollection, content, id)
                            }
                        }
                    }

                    private fun checkIfNumber(content: kotlin.String, regexCollection: RegexCollection): Variable.Value.Types? {
                        var result = regexCollection.bin.matchEntire(content)
                        if (result != null) {
                            return Variable.Value.Types.Bin
                        }
                        result = regexCollection.hex.matchEntire(content)
                        if (result != null) {
                            return Variable.Value.Types.Hex
                        }
                        result = regexCollection.udec.matchEntire(content)
                        if (result != null) {
                            return Variable.Value.Types.UDec
                        }
                        result = regexCollection.dec.matchEntire(content)
                        if (result != null) {
                            return Variable.Value.Types.Dec
                        }
                        return null
                    }
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

    enum class CodeStyle(val lightHexColor: Int,val darkHexColor: Int? = null) {
        RED(0xc94922),
        ORANGE(0xc76b29),
        YELLOW(0xc08b30),
        GREEN(0xac9739),
        GREENPC(0x008b19),
        CYAN(0x22a2c9),
        BLUE(0x3d8fd1),
        VIOLET(0x6679cc),
        MAGENTA(0x9c637a),
        BASE0(0x202746,0xf5f7ff),
        BASE1(0x293256,0xdfe2f1),
        BASE2(0x5e6687,0x979db4),
        BASE3(0x6b7394,0x898ea4),
        BASE4(0x898ea4,0x6b7394),
        BASE5(0x979db4,0x5e6687),
        BASE6(0xdfe2f1,0x293256),
        BASE7(0xf5f7ff,0x202746),
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

    data class LineLoc(val file: FileHandler.File, var lineID: Int, val startIndex: Int, val endIndex: Int)
// endIndex means index after last Character
}