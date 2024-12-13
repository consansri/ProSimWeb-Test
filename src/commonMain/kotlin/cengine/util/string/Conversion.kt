package cengine.util.string




fun String.asciiToHexString(): String {
    return this.map {
        it.code.toString(16).padStart(2, '0')
    }.joinToString("") { it }
}