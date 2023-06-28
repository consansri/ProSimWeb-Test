package extendable.components.assembly

import extendable.ArchConst
import extendable.Architecture
import extendable.components.connected.RegisterContainer

class Assembly(private val architecture: Architecture, private val grammar: Grammar, private val regexCollection: RegexCollection, private val hlFlagCollection: HLFlagCollection) {

    private var tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private var dryLines: List<String>? = null
    private var hlLines: MutableList<String>? = null
    private var dryContent = ""
    private var grammarTree: Grammar.GrammarTree? = null

    private fun initCode(code: String) {
        tokenList = mutableListOf()
        tokenLines = mutableListOf()
        dryContent = code
        dryLines = dryContent.split(*ArchConst.LINEBREAKS.toTypedArray())
        hlLines = dryLines?.toMutableList()
    }

    fun setCode(code: String, shouldHighlight: Boolean) {
        initCode(code)
        analyze()
        parse()
        if(shouldHighlight){
            highlight()
        }
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
                        tokenList += Token.Space(LineLoc(lineID, startIndex, startIndex + space.value.length), space.value)
                        tempTokenList += Token.Space(LineLoc(lineID, startIndex, startIndex + space.value.length), space.value)
                        startIndex += space.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val binary = regexCollection.binary.find(remainingLine)
                    if (binary != null) {
                        tokenList += Token.Constant.Binary(LineLoc(lineID, startIndex, startIndex + binary.value.length), binary.value)
                        tempTokenList += Token.Constant.Binary(LineLoc(lineID, startIndex, startIndex + binary.value.length), binary.value)
                        startIndex += binary.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val hex = regexCollection.hex.find(remainingLine)
                    if (hex != null) {
                        tokenList += Token.Constant.Hex(LineLoc(lineID, startIndex, startIndex + hex.value.length), hex.value)
                        tempTokenList += Token.Constant.Hex(LineLoc(lineID, startIndex, startIndex + hex.value.length), hex.value)
                        startIndex += hex.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val dec = regexCollection.dec.find(remainingLine)
                    if (dec != null) {
                        tokenList += Token.Constant.Dec(LineLoc(lineID, startIndex, startIndex + dec.value.length), dec.value)
                        tempTokenList += Token.Constant.Dec(LineLoc(lineID, startIndex, startIndex + dec.value.length), dec.value)
                        startIndex += dec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val udec = regexCollection.udec.find(remainingLine)
                    if (udec != null) {
                        tokenList += Token.Constant.UDec(LineLoc(lineID, startIndex, startIndex + udec.value.length), udec.value)
                        tempTokenList += Token.Constant.UDec(LineLoc(lineID, startIndex, startIndex + udec.value.length), udec.value)
                        startIndex += udec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val ascii = regexCollection.ascii.find(remainingLine)
                    if(ascii != null){
                        tokenList += Token.Constant.Ascii(LineLoc(lineID, startIndex, startIndex + ascii.value.length), ascii.value)
                        tempTokenList += Token.Constant.Ascii(LineLoc(lineID, startIndex, startIndex + ascii.value.length), ascii.value)
                        startIndex += ascii.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val symbol = regexCollection.symbol.find(remainingLine)
                    if (symbol != null) {
                        tokenList += Token.Symbol(LineLoc(lineID, startIndex, startIndex + symbol.value.length), symbol.value)
                        tempTokenList += Token.Symbol(LineLoc(lineID, startIndex, startIndex + symbol.value.length), symbol.value)
                        startIndex += symbol.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val regRes = regexCollection.alphaNumeric.find(remainingLine)
                    if (regRes != null) {
                        val reg = architecture.getRegisterContainer().getRegister(regRes.value)
                        if (reg != null) {
                            tokenList += Token.Register(LineLoc(lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg)
                            tempTokenList += Token.Register(LineLoc(lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg)
                            startIndex += regRes.value.length
                            remainingLine = line.substring(startIndex)
                            continue

                        }
                    }

                    val insRes = regexCollection.word.find(remainingLine)
                    if (insRes != null) {
                        val ins = architecture.findInstruction(insRes.value)
                        if (ins != null) {
                            tokenList += Token.Instruction(LineLoc(lineID, startIndex, startIndex + insRes.value.length), insRes.value, ins)
                            tempTokenList += Token.Instruction(LineLoc(lineID, startIndex, startIndex + insRes.value.length), insRes.value, ins)
                            startIndex += insRes.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    }

                    // apply rest
                    val alphaNumeric = regexCollection.alphaNumeric.find(remainingLine)
                    val word = regexCollection.word.find(remainingLine)

                    if (alphaNumeric != null && word != null) {
                        if (alphaNumeric.value.length == word.value.length) {
                            tokenList += Token.Word(LineLoc(lineID, startIndex, startIndex + word.value.length), word.value)
                            tempTokenList += Token.Word(LineLoc(lineID, startIndex, startIndex + word.value.length), word.value)
                            startIndex += word.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        } else {
                            tokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value)
                            tempTokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    } else {
                        if (alphaNumeric != null) {
                            tokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value)
                            tempTokenList += Token.AlphaNum(LineLoc(lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    }

                    architecture.getConsole().warn("Assembly: no match found for $remainingLine")
                    break;
                }
                tokenLines.add(lineID, tempTokenList)
                tokenList += Token.NewLine(LineLoc(lineID, line.length, line.length + 2), "\n")
            }
        }

    }

    private fun parse() {
        grammar.clear()
        grammarTree = grammar.check(tokenLines)
    }

    private fun highlight() {

        for (lineID in tokenLines.indices) {
            val tokenLine = tokenLines[lineID]
            var hlLine = ""

            for (token in tokenLine) {
                if(grammarTree?.nodes != null){
                    val node = grammarTree?.contains(token)
                    if (node != null) {
                        if(node.hlFlag.isNotEmpty()){
                            token.hl(architecture, node.hlFlag)
                            hlLine += token.hlContent
                            continue
                        }
                    }
                }

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

                    is Token.Instruction -> {
                        hlFlagCollection.instruction?.let {
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
                hlLine += token.hlContent
            }

            hlLines?.let {
                it[lineID] = hlLine
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

    sealed class Token(val lineLoc: LineLoc, val content: String) {

        var hlContent = content
        abstract val type: TokenType

        fun hl(architecture: Architecture, hlFlag: String) {
            hlContent = architecture.highlight(content, hlFlag)
        }

        override fun toString(): String {
            return content
        }

        class NewLine(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            override val type = TokenType.NEWLINE
        }

        class Space(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            override val type = TokenType.SPACE
        }

        class Symbol(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            override val type = TokenType.SYMBOL
        }

        sealed class Constant(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            override val type = TokenType.CONSTANT

            class Ascii(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class Binary(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class Hex(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class Dec(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class UDec(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
        }

        class Register(lineLoc: LineLoc, content: String, reg: RegisterContainer.Register) : Token(lineLoc, content) {
            override val type = TokenType.REGISTER
        }

        class Instruction(lineLoc: LineLoc, content: String, ins: extendable.components.connected.Instruction) : Token(lineLoc, content) {
            override val type = TokenType.INSTRUCTION
        }

        class AlphaNum(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            override val type = TokenType.ALPHANUM
        }

        class Word(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
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