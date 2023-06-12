package extendable.components.types

class OpCode(private var opMask: String, private var opLabels: List<String>, private var splitSymbol: String) {

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

    fun getBinaryString(): String {
        return binaryString
    }

    fun getDecimal(): Int {
        return decimal
    }

    fun getHexString(): String {
        return decimal.toString(16).uppercase()
    }


}