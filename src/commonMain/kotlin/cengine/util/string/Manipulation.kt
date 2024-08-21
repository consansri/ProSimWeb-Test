package cengine.util.string

/**
 *  This Object contains some often needed [String] helping functions.
 */

private val leadingZerosRegex = Regex("^0+(?!$)")

fun String.removeLeadingZeros(): String = replaceFirst(leadingZerosRegex, "").ifEmpty { "0" }
fun String.splitAtFirstOccurrence(delimiter: Char): Pair<String, String> {
    val indexOfDelimiter = indexOf(delimiter)
    return if (indexOfDelimiter != -1) {
        val firstPart = substring(0, indexOfDelimiter)
        val secondPart = substring(indexOfDelimiter)
        Pair(firstPart, secondPart)
    } else {
        // If the character was not found, return the original string as the first part
        Pair(this, "")
    }
}
