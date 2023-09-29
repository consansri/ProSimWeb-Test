package emulator.kit.types

object StringTools {
    fun splitStringAtFirstOccurrence(inputString: String, delimiter: Char): Pair<String, String> {
        val indexOfDelimiter = inputString.indexOf(delimiter)
        return if (indexOfDelimiter != -1) {
            val firstPart = inputString.substring(0, indexOfDelimiter)
            val secondPart = inputString.substring(indexOfDelimiter)
            console.warn("found comment beginning: $firstPart, ${secondPart}")
            Pair(firstPart, secondPart)
        } else {
            // Falls das Zeichen nicht gefunden wurde, gib die ursprüngliche Zeichenkette als ersten Teil zurück
            Pair(inputString, "")
        }
    }

}