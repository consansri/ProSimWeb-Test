package cengine.util.integer

interface IntNumberStatic<out T: IntNumber<*>> {

    val BYTES: Int
    val BITS: Int
    val ONE: T
    val ZERO: T


    fun to(number: IntNumber<*>): T
    fun split(number: IntNumber<*>): List<T>
    fun of(value: Int): T
    fun parse(string: String, radix: Int): T
    fun createBitMask(bitWidth: Int): T

}