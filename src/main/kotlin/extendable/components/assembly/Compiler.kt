package extendable.components.assembly

import extendable.ArchConst
import extendable.Architecture
import extendable.components.connected.RegisterContainer
import extendable.components.types.ByteValue
import tools.DebugTools

class Compiler(private val architecture: Architecture, private val grammar: Grammar, private val assembly: Assembly, private val regexCollection: RegexCollection, private val hlFlagCollection: HLFlagCollection) {

    private var tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private var dryLines: List<String>? = null
    private var hlLines: MutableList<String>? = null
    private var dryContent = ""
    private var grammarTree: Grammar.GrammarTree? = null
    private var isBuildable = false
    private var reservationMap: Assembly.ReservationMap = Assembly.ReservationMap()

    private fun initCode(code: String) {
        tokenList = mutableListOf()
        tokenLines = mutableListOf()
        dryContent = code
        dryLines = dryContent.split(*ArchConst.LINEBREAKS.toTypedArray())
        hlLines = dryLines?.toMutableList()
    }

    fun isBuildable(): Boolean {
        return isBuildable
    }

    fun getReservationMap(): Assembly.ReservationMap{
        return reservationMap
    }

    fun setCode(code: String, shouldHighlight: Boolean) {
        initCode(code)
        analyze()
        parse()
        if (shouldHighlight) {
            highlight()
        }
        compile()
    }

    fun recompile(){
        compile()
    }

    private fun analyze() {

        dryLines?.let {
            for (lineID in it.indices) {
                val line = it[lineID]
                val tempTokenList = mutableListOf<Token>()
                var remainingLine = line
                var startIndex = 0

                while (remainingLine.isNotEmpty()) {
                    val space = regexCollection.space.find(remainingLine)
                    if (space != null) {
                        tokenList += Token.Space(LineLoc(lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                        tempTokenList += Token.Space(LineLoc(lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                        startIndex += space.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val binary = regexCollection.binary.find(remainingLine)
                    if (binary != null) {
                        tokenList += Token.Constant.Binary(LineLoc(lineID, startIndex, startIndex + binary.value.length), binary.value, tokenList.size)
                        tempTokenList += Token.Constant.Binary(LineLoc(lineID, startIndex, startIndex + binary.value.length), binary.value, tokenList.size)
                        startIndex += binary.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val hex = regexCollection.hex.find(remainingLine)
                    if (hex != null) {
                        tokenList += Token.Constant.Hex(LineLoc(lineID, startIndex, startIndex + hex.value.length), hex.value, tokenList.size)
                        tempTokenList += Token.Constant.Hex(LineLoc(lineID, startIndex, startIndex + hex.value.length), hex.value, tokenList.size)
                        startIndex += hex.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val dec = regexCollection.dec.find(remainingLine)
                    if (dec != null) {
                        tokenList += Token.Constant.Dec(LineLoc(lineID, startIndex, startIndex + dec.value.length), dec.value, tokenList.size)
                        tempTokenList += Token.Constant.Dec(LineLoc(lineID, startIndex, startIndex + dec.value.length), dec.value, tokenList.size)
                        startIndex += dec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val udec = regexCollection.udec.find(remainingLine)
                    if (udec != null) {
                        tokenList += Token.Constant.UDec(LineLoc(lineID, startIndex, startIndex + udec.value.length), udec.value, tokenList.size)
                        tempTokenList += Token.Constant.UDec(LineLoc(lineID, startIndex, startIndex + udec.value.length), udec.value, tokenList.size)
                        startIndex += udec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val ascii = regexCollection.ascii.find(remainingLine)
                    if (ascii != null) {
                        tokenList += Token.Constant.Ascii(LineLoc(lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                        tempTokenList += Token.Constant.Ascii(LineLoc(lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                        startIndex += ascii.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val symbol = regexCollection.symbol.find(remainingLine)
                    if (symbol != null) {
                        tokenList += Token.Symbol(LineLoc(lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                        tempTokenList += Token.Symbol(LineLoc(lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                        startIndex += symbol.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val regRes = regexCollection.alphaNumeric.find(remainingLine)
                    if (regRes != null) {
                        val reg = architecture.getRegisterContainer().getRegister(regRes.value)
                        if (reg != null) {
                            tokenList += Token.Register(LineLoc(lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                            tempTokenList += Token.Register(LineLoc(lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                            startIndex += regRes.value.length
                            remainingLine = line.substring(startIndex)
                            continue

                        }
                    }

                    // apply rest
                    val alphaNumeric = regexCollection.alphaNumeric.find(remainingLine)
                    val word = regexCollection.word.find(remainingLine)

                    if (alphaNumeric != null && word != null) {
                        if (alphaNumeric.value.length == word.value.length) {
                            tokenList += Token.Word(LineLoc(lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
                            tempTokenList += Token.Word(LineLoc(lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
                            startIndex += word.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        } else {
                            tokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            tempTokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    } else {
                        if (alphaNumeric != null) {
                            tokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            tempTokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    }

                    architecture.getConsole().warn("Assembly: no match found for $remainingLine")
                    break;
                }
                tokenLines.add(lineID, tempTokenList)
                tokenList += Token.NewLine(LineLoc(lineID, line.length, line.length + 2), "\n", tokenList.size)
            }
        }

    }

    private fun parse() {
        grammar.clear()
        grammarTree = grammar.check(tokenLines)
        grammarTree?.rootNode?.allErrors?.let {
            architecture.getConsole().clear()
            for (error in it) {
                if (error.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    architecture.getConsole().error("line ${error.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Error {NodeType: ${error.linkedTreeNode.name}, Tokens: ${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}} \n${error.message}")
                } else {
                    architecture.getConsole().error("GlobalError: " + error.message)
                }
            }
            if (it.isEmpty()) {
                architecture.getConsole().log("build successful!")
                isBuildable = true
            } else {
                isBuildable = false
            }
        }
    }

    private fun highlight() {

        for (lineID in tokenLines.indices) {
            val tokenLine = tokenLines[lineID]
            var hlLine = ""

            for (token in tokenLine) {
                if (grammarTree?.rootNode != null) {
                    val node = grammarTree?.contains(token)
                    if (node != null) {
                        if (node.hlFlag.isNotEmpty()) {
                            token.hl(architecture, node.hlFlag, node.name)
                            hlLine += token.hlContent
                            continue
                        }
                    }
                    if (grammarTree?.errorsContain(token) == true) {
                        token.hl(architecture, ArchConst.StandardHL.error, "error")
                        hlLine += token.hlContent
                        continue
                    }
                }

                if (grammar.applyStandardHLForRest) {
                    when (token) {
                        is Token.AlphaNum -> {
                            hlFlagCollection.alphaNum?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Binary -> {
                            hlFlagCollection.const_bin?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Dec -> {
                            hlFlagCollection.const_dec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Hex -> {
                            hlFlagCollection.const_hex?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.UDec -> {
                            hlFlagCollection.const_udec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Ascii -> {
                            hlFlagCollection.const_ascii?.let {
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

    private fun compile() {
        architecture.getMemory().clear()
        if (isBuildable) {
            grammarTree?.let {
                reservationMap = assembly.generateByteCode(architecture, it)
                assembly.generateTranscript(architecture, it)
                architecture.getRegisterContainer().pc.value.setHex("0")
            }
        }
    }

    fun getHLContent(): String {
        val stringBuilder = StringBuilder()
        hlLines?.let {
            for (line in it) {
                stringBuilder.append("$line\n")
            }
        }
        val hlContent = stringBuilder.toString()
        if (hlContent.isNotEmpty()) {
            return hlContent
        } else {
            return dryContent
        }
    }

    fun getGrammarTree(): Grammar.GrammarTree? {
        return grammarTree
    }

    sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {

        var hlContent = content
        abstract val type: TokenType

        fun hl(architecture: Architecture, hlFlag: String, title: String = "") {
            hlContent = architecture.highlight(content, id, title, hlFlag, "token")
        }

        override fun toString(): String {
            return content
        }

        class NewLine(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.NEWLINE
        }

        class Space(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SPACE
        }

        class Symbol(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SYMBOL
        }

        sealed class Constant(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.CONSTANT
            abstract fun getValue(): ByteValue.Type

            class Ascii(lineLoc: LineLoc, content: String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): ByteValue.Type {
                    val binChars = StringBuilder()
                    for (char in content) {
                        val bin = char.digitToInt().toString(2)
                        binChars.append(bin)
                    }
                    return ByteValue.Type.Binary(binChars.toString())
                }
            }

            class Binary(lineLoc: LineLoc, content: String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): ByteValue.Type {
                    return ByteValue.Type.Binary(content)
                }
            }

            class Hex(lineLoc: LineLoc, content: String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): ByteValue.Type {
                    return ByteValue.Type.Hex(content).toBin()
                }
            }

            class Dec(lineLoc: LineLoc, content: String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): ByteValue.Type {
                    if (DebugTools.ARCH_showCompilerInfo) {
                        console.warn("Compiler.Dec.getValue(): Bottleneck of maximum input is set to 32 Bit caused by missing getNearestSize for decimal Values!")
                    }
                    return ByteValue.Type.Dec(content, ByteValue.Size.Bit32())
                }
            }

            class UDec(lineLoc: LineLoc, content: String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): ByteValue.Type {
                    if (DebugTools.ARCH_showCompilerInfo) {
                        console.warn("Compiler.UDec.getValue(): Bottleneck of maximum input is set to 32 Bit caused by missing getNearestSize for decimal Values!")
                    }
                    return ByteValue.Type.UDec(content, ByteValue.Size.Bit32())
                }
            }
        }

        class Register(lineLoc: LineLoc, content: String, val reg: RegisterContainer.Register, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.REGISTER
        }

        class AlphaNum(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.ALPHANUM
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
        INSTRUCTION,
        ALPHANUM,
        WORD,
        ANY
    }

    data class HLFlagCollection(
        val alphaNum: String? = null,
        val word: String? = null,
        val const_hex: String? = null,
        val const_bin: String? = null,
        val const_dec: String? = null,
        val const_udec: String? = null,
        val const_ascii: String? = null,
        val register: String? = null,
        val symbol: String? = null,
        val instruction: String? = null,
        val comment: String? = null,
        val whitespace: String? = null
    )

    data class RegexCollection(
        val space: Regex,
        val symbol: Regex,
        val binary: Regex,
        val hex: Regex,
        val dec: Regex,
        val udec: Regex,
        val ascii: Regex,
        val alphaNumeric: Regex,
        val word: Regex,
    )

    data class LineLoc(val lineID: Int, val startIndex: Int, val endIndex: Int)

    // endIndex means index after last Character
    data class CompilationResult(val highlightedContent: String, val buildable: Boolean)


}