package extendable.components.types

import extendable.ArchConst

class OpCode(private var opMask: String, private var opLabels: List<OpLabel>, private var splitSymbol: String) {

    private val binaryString: String
    private val decimal: Int

    init {
        opMask = opMask.removePrefix("0b")
        val regex = Regex("[01]+")
        val matches = regex.findAll(opMask)
        val stringBuilder = StringBuilder()
        for (match in matches) {
            stringBuilder.append(match.value)
        }
        binaryString = stringBuilder.toString()
        decimal = binaryString.toInt(2)
    }

    fun getOpCode(replaceLabels: Map<String, String>): String? {
        var splittedOpMask = opMask.split(splitSymbol).toMutableList()
        for (labelID in splittedOpMask.indices) {
            if (!opLabels[labelID].static) {
                replaceLabels[opLabels[labelID].name]?.let {
                    if (splittedOpMask[labelID].length == it.length) {
                        splittedOpMask[labelID] = it
                    } else {
                        return null
                    }
                }
            }
        }
        return splittedOpMask.joinToString()
    }

    fun compareOpMaskLastDigits(binaryString: String, n: Int): Boolean {
        var same = true
        val filteredBinStr = binaryString.removePrefix(ArchConst.PRESTRING_BINARY)
        if (filteredBinStr.takeLast(n) != this.binaryString.takeLast(n)) {
            same = false
        }
        return same
    }

    fun compareOpMaskFirstDigits(binaryString: String, n: Int): Boolean {
        var same = true
        val filteredBinStr = binaryString.removePrefix(ArchConst.PRESTRING_BINARY)
        for (digit in 0 until n) {
            if (filteredBinStr[digit] != this.binaryString[digit]) {
                same = false
            }
        }
        return same
    }

    fun compareOpMaskSpecificDigits(binaryString: String, range: IntRange): Boolean {
        var same = true
        val filteredBinStr = binaryString.removePrefix(ArchConst.PRESTRING_BINARY)
        for (digit in range) {
            if (filteredBinStr[digit] != this.binaryString[digit]) {
                same = false
            }
        }
        return same
    }

    fun getBinaryString(): String {
        return binaryString
    }

    fun getDecimal(): Int {
        return decimal
    }

    fun getHexString(): String {
        return decimal.toString(16).uppercase()
    }

    class OpLabel(val name: String, val type: String?, val static: Boolean)


}