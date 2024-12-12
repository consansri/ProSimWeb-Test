package cengine.util.newint

interface IntNumberStatic<out T: IntNumber<*>> {

    val ONE: T
    val ZERO: T

    fun to(number: IntNumber<*>): T
    fun split(number: IntNumber<*>): List<T>
    fun of(value: Int): T
    fun parse(string: String, radix: Int): T
    fun createBitMask(bitWidth: Int): T

}