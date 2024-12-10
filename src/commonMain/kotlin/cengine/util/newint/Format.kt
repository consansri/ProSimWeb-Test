package cengine.util.newint

import com.ionspin.kotlin.bignum.integer.BigInteger

enum class Format(val radix: Int) {
    BIN(2) {
        override fun format(number: IntNumber<*>): String = number.zeroPaddedBin()
        override fun valid(char: Char): Boolean = when (char) {
            '1', '0' -> true
            else -> false
        }
    },
    OCT(8) {
        override fun format(number: IntNumber<*>): String = number.toString(8)
        override fun valid(char: Char): Boolean = when (char) {
            in '0'..'7' -> true
            else -> false
        }
    },
    DEC(10) {
        override fun format(number: IntNumber<*>): String = number.toString()
        override fun valid(char: Char): Boolean = char.isDigit()
    },
    HEX(16) {
        override fun format(number: IntNumber<*>): String = number.zeroPaddedHex()
        override fun valid(char: Char): Boolean = when (char) {
            in '0'..'9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f' -> true
            else -> false
        }
    };

    fun next(): Format {
        val length = entries.size
        val currIndex = entries.indexOf(this)
        val nextIndex = (currIndex + 1) % length
        return entries[nextIndex]
    }

    abstract fun format(number: IntNumber<*>): String

    abstract fun valid(char: Char): Boolean

    fun filter(string: String): String = string.filter { valid(it) }

    fun parse(string: String): IntNumber<*> = BigInt(BigInteger.parseString(string, radix))

}