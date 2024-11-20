package cengine.util.string


fun Array<Byte>.hexDump(): String = joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }

fun String.asciiToHexString(): String {
    return this.map {
        it.code.toString(16).padStart(2, '0')
    }.joinToString("") { it }
}