package cengine.util.newint

interface LogicOperationProvider<in T: IntNumber<*>, out U: IntNumber<*>> {

    val bitWidth: Int

    operator fun inv(): U
    infix fun and(other: T): U
    infix fun or(other: T): U
    infix fun xor(other: T): U
    infix fun shl(bits: T): U
    infix fun shr(bits: T): U

    // Kotlin Int Operations
    infix fun and(other: Int): U
    infix fun or(other: Int): U
    infix fun xor(other: Int): U
    infix fun shl(bits: Int): U
    infix fun shr(bits: Int): U


    // Kotlin Int Operations
    infix fun and(other: Long): U
    infix fun or(other: Long): U
    infix fun xor(other: Long): U

}

