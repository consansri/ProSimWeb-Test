package extendable.components.assembly

import extendable.ArchConst
import extendable.Architecture
import extendable.components.connected.RegisterContainer
import extendable.components.types.MutVal
import tools.HTMLTools

class Compiler(private val architecture: Architecture, private val grammar: Grammar, private val assembly: Assembly, private val regexCollection: RegexCollection, private val hlFlagCollection: HLFlagCollection) {

    private var tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private var dryLines: List<String>? = null
    private var hlLines: MutableList<String>? = null
    private var dryContent = ""
    private var grammarTree: Grammar.GrammarTree? = null
    private var isBuildable = false
    private var assemblyMap: Assembly.AssemblyMap = Assembly.AssemblyMap()

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

    fun getAssemblyMap(): Assembly.AssemblyMap {
        return assemblyMap
    }

    fun setCode(code: String, shouldHighlight: Boolean): Boolean {
        initCode(code)
        analyze()
        parse()
        if (shouldHighlight) {
            highlight()
        }
        compile()
        return isBuildable
    }

    fun recompile() {
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

                    val string = regexCollection.string.find(remainingLine)
                    if (string != null) {
                        tokenList += Token.Constant.String(LineLoc(lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                        tempTokenList += Token.Constant.String(LineLoc(lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                        startIndex += string.value.length
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
        grammarTree = grammar.check(this, tokenLines, architecture.getFileHandler().getAllFiles())
        architecture.getConsole().clear()
        grammarTree?.rootNode?.allWarnings?.let {
            for (warning in it) {
                if (warning.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if(warning.linkedTreeNode.getAllTokens().first().isPseudo()){
                        architecture.getConsole().warn("pseudo: Warning ${warning.message}")
                    }else{
                        architecture.getConsole().warn("line ${warning.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Warning ${warning.message}")
                    }
                } else {
                    architecture.getConsole().error("GlobalWarning: " + warning.message)
                }
            }
        }

        grammarTree?.rootNode?.allErrors?.let {
            for (error in it) {
                if (error.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if(error.linkedTreeNode.getAllTokens().first().isPseudo()){
                        architecture.getConsole().error("pseudo: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                    }else{
                        architecture.getConsole().error("line ${error.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString("") { it.content }}]")
                    }
                } else {
                    architecture.getConsole().error("GlobalError: " + error.message)
                }
            }
            isBuildable = it.isEmpty()
            if (isBuildable) architecture.getConsole().log("build successful!")
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
                        val hlFlag = node.highlighting.getHLFlag(token)
                        if (hlFlag != null) {
                            token.hl(architecture, hlFlag, node.name)
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

                        is Token.Constant.String -> {
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
        architecture.getRegisterContainer().pc.reset()
        if (isBuildable) {
            grammarTree?.let {
                assemblyMap = assembly.generateByteCode(architecture, it)
                assembly.generateTranscript(architecture, it)
                architecture.getFileHandler().getCurrent().linkGrammarTree(it)
            }
        }
    }
    fun pseudoAnalyze(content: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var remaining = content
        var startIndex = 0

        while (remaining.isNotEmpty()) {
            val space = regexCollection.space.find(remaining)
            if (space != null) {
                tokens += Token.Space(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + space.value.length), space.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += space.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val binary = regexCollection.binary.find(remaining)
            if (binary != null) {
                tokens += Token.Constant.Binary(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + binary.value.length), binary.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += binary.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                tokens += Token.Constant.Hex(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + hex.value.length), hex.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += hex.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                tokens += Token.Constant.Dec(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + dec.value.length), dec.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += dec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                tokens += Token.Constant.UDec(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + udec.value.length), udec.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += udec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                tokens += Token.Constant.Ascii(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + ascii.value.length), ascii.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += ascii.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                tokens += Token.Constant.String(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + string.value.length), string.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += string.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val symbol = regexCollection.symbol.find(remaining)
            if (symbol != null) {
                tokens += Token.Symbol(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + symbol.value.length), symbol.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                startIndex += symbol.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val regRes = regexCollection.alphaNumeric.find(remaining)
            if (regRes != null) {
                val reg = architecture.getRegisterContainer().getRegister(regRes.value)
                if (reg != null) {
                    tokens += Token.Register(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + regRes.value.length), regRes.value, reg, ArchConst.COMPILER_TOKEN_PSEUDOID)
                    startIndex += regRes.value.length
                    remaining = content.substring(startIndex)
                    continue

                }
            }

            // apply rest
            val alphaNumeric = regexCollection.alphaNumeric.find(remaining)
            val word = regexCollection.word.find(remaining)

            if (alphaNumeric != null && word != null) {
                if (alphaNumeric.value.length == word.value.length) {
                    tokens += Token.Word(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + word.value.length), word.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                    startIndex += word.value.length
                    remaining = content.substring(startIndex)
                    continue
                } else {
                    tokens += Token.AlphaNum(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                    startIndex += alphaNumeric.value.length
                    remaining = content.substring(startIndex)
                    continue
                }
            } else {
                if (alphaNumeric != null) {
                    tokens += Token.AlphaNum(LineLoc(ArchConst.COMPILER_TOKEN_PSEUDOID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, ArchConst.COMPILER_TOKEN_PSEUDOID)
                    startIndex += alphaNumeric.value.length
                    remaining = content.substring(startIndex)
                    continue
                }
            }

            architecture.getConsole().warn("Assembly.analyze($content): no match found for $remaining")
            break;
        }

        return tokens
    }
    fun getHLContent(): String {
        val stringBuilder = StringBuilder()
        hlLines?.let {
            for (line in it) {
                stringBuilder.append("$line\n")
            }
        }
        val hlContent = stringBuilder.toString()
        return if (hlContent.isNotEmpty()) {
            hlContent
        } else {
            dryContent
        }
    }
    fun getGrammarTree(): Grammar.GrammarTree? {
        return grammarTree
    }

    sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {

        var hlContent = content
        abstract val type: TokenType

        fun isPseudo(): Boolean {
            return id == ArchConst.COMPILER_TOKEN_PSEUDOID
        }

        fun hl(architecture: Architecture, hlFlag: String, title: String = "") {
            hlContent = architecture.highlight(HTMLTools.encodeHTML(content), id, title, hlFlag, "token")
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

        sealed class Constant(lineLoc: LineLoc, content: kotlin.String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.CONSTANT
            abstract fun getValue(): MutVal.Value

            class Ascii(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {
                    val binChars = StringBuilder()
                    val byteArray = content.substring(1, content.length - 1).encodeToByteArray()
                    for (byte in byteArray) {
                        val bin = byte.toInt().toString(2)
                        binChars.append(bin)
                    }
                    return MutVal.Value.Binary(binChars.toString())
                }
            }

            class String(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {
                    val hexStr = StringBuilder()
                    val trimmedContent = content.substring(1, content.length - 1)
                    for (char in trimmedContent) {
                        val hexChar = char.code.toString(16)
                        hexStr.append(hexChar)
                    }
                    return MutVal.Value.Hex(hexStr.toString())
                }
            }

            class Binary(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {
                    return MutVal.Value.Binary(content)
                }
            }

            class Hex(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {
                    return MutVal.Value.Hex(content).toBin()
                }
            }

            class Dec(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {
                    return MutVal.Value.Dec(content)
                }
            }

            class UDec(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): MutVal.Value {

                    return MutVal.Value.UDec(content)
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
        val const_string: String? = null,
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
        val string: Regex,
        val alphaNumeric: Regex,
        val word: Regex,
    )

    data class LineLoc(val lineID: Int, val startIndex: Int, val endIndex: Int)
    // endIndex means index after last Character

}