package cengine.util.string

/**
 *  This Object contains some often needed [String] helping functions.
 */

private val leadingZerosRegex = Regex("^0+(?!$)")

fun String.removeLeadingZeros(): String = replaceFirst(leadingZerosRegex, "").ifEmpty { "0" }
