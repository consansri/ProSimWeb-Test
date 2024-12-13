package cengine.util.integer


inline fun <reified T : IntNumber<*>> Array<T>.hexDump(): String = joinToString(" ") { it.toUnsigned().zeroPaddedHex() }

