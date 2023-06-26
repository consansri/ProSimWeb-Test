package extendable.components.assembly

import extendable.ArchConst
import extendable.Architecture
import extendable.components.connected.RegisterContainer

class Assembly(private val architecture: Architecture, private val dryContent: String, private val regexCollection: RegexCollection, private val hlFlagCollection: HLFlagCollection, vararg val option: AssemblyOption) {

    private val tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private val dryLines: List<String> = dryContent.split(*ArchConst.LINEBREAKS.toTypedArray())
    private var hlLines: MutableList<String> = dryLines.toMutableList()


    init {
        analyze()

        parse()
        highlight()
    }

    private fun analyze() {

        for (lineID in dryLines.indices) {
            val line = dryLines[lineID]
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
        }
    }

    private fun parse() {

    }

    private fun highlight() {
        for(lineID in tokenLines.indices){
            val tokenLine = tokenLines[lineID]
            var hlLine = ""
            for(token in tokenLine){
                when(token){
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
                    is Token.Space -> {

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
                }
                hlLine += token.hlContent
            }
            hlLines[lineID] = hlLine
        }
    }

    fun getHLContent(): String {
        val stringBuilder = StringBuilder()
        for (line in hlLines) {
            stringBuilder.append("$line\n")
        }
        return stringBuilder.toString()
    }

    sealed class AssemblyOption {
        data class OptSplitParam(val splitSymbol: String) : AssemblyOption()
    }

    sealed class Token(val lineLoc: LineLoc, val content: String) {

        var hlContent = content

        fun hl(architecture: Architecture, hlFlag: String){
            hlContent = architecture.highlight(content, hlFlag)
        }

        override fun toString(): String {
            return content
        }

        class Space(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {

        }

        class Symbol(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {

        }

        sealed class Constant(lineLoc: LineLoc, content: String) : Token(lineLoc, content) {
            class Binary(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class Hex(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class Dec(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
            class UDec(lineLoc: LineLoc, content: String) : Constant(lineLoc, content)
        }

        class Register(lineLoc: LineLoc, content: String, reg: RegisterContainer.Register) : Token(lineLoc, content)
        class Instruction(lineLoc: LineLoc, content: String, ins: extendable.components.connected.Instruction) : Token(lineLoc, content)
        class AlphaNum(lineLoc: LineLoc, content: String) : Token(lineLoc, content)
        class Word(lineLoc: LineLoc, content: String) : Token(lineLoc, content)

    }

    data class HLFlagCollection(
        val alphaNum: String?,
        val word: String?,
        val const_hex: String?,
        val const_bin: String?,
        val const_dec: String?,
        val const_udec: String?,
        val register: String?,
        val symbol: String?,
        val instruction: String?,
        val comment: String?
    )

    data class RegexCollection(
        val space: Regex,
        val symbol: Regex,
        val binary: Regex,
        val hex: Regex,
        val dec: Regex,
        val udec: Regex,
        val alphaNumeric: Regex,
        val word: Regex,
    )

    data class LineLoc(val lineID: Int, val startIndex: Int, val endIndex: Int)

    // endIndex means index after last Character
    data class CompilationResult(val highlightedContent: String, val buildable: Boolean)


}