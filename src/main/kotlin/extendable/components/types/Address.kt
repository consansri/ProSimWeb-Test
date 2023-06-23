package extendable.components.types

import extendable.ArchConst
import kotlin.math.pow

@Deprecated("Not skalable with maximum addresses of 64bit caused by Long")
class Address(private val value: Long, private val width: Int) {

    init {
        require(width in 1..128) { "Bit width must be between 1 and 128" }
        require(value in 0..2.0.pow(width).toInt() || value == ArchConst.ADDRESS_NOVALUE) { "Value is out of valid range for the given bit width" }
    }

    fun getHex(): String {
        return ArchConst.PRESTRING_HEX + value.toString(16).padStart(width / 4, '0')
    }

    fun getBinary(): String {
        return ArchConst.PRESTRING_BINARY + value.toString(2).padStart(width, '0')
    }

    fun getValue(): Long {
        return value
    }

}