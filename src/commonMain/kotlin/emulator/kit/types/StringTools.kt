package emulator.kit.types

/**
 *  This Object contains some often needed [String] helping functions.
 */
object StringTools {
    private val leadingZerosRegex = Regex("^0+(?!$)")
    fun removeLeadingZeros(inputString: String): String = inputString.replaceFirst(leadingZerosRegex, "").ifEmpty { "0" }
    fun splitStringAtFirstOccurrence(inputString: String, delimiter: Char): Pair<String, String> {
        val indexOfDelimiter = inputString.indexOf(delimiter)
        return if (indexOfDelimiter != -1) {
            val firstPart = inputString.substring(0, indexOfDelimiter)
            val secondPart = inputString.substring(indexOfDelimiter)
            Pair(firstPart, secondPart)
        } else {
            // If the character was not found, return the original string as the first part
            Pair(inputString, "")
        }
    }

}