package extendable.components.types

class OpCode(val opString: String) {

    private val binaryString: String
    private val decimal: Int


    init {
        val regex = Regex("[01]+")
        val matches = regex.findAll(opString)
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