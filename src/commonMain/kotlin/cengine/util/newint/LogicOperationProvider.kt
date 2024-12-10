package cengine.util.newint

interface LogicOperationProvider<in T: Any, out U: Any> {

    operator fun inv(): U
    infix fun and(other: T): U
    infix fun or(other: T): U
    infix fun xor(other: T): U
    infix fun shl(other: T): U
    infix fun shr(other: T): U

    // Kotlin Int Operations
    infix fun and(other: Int): U
    infix fun or(other: Int): U
    infix fun xor(other: Int): U
    infix fun shl(other: Int): U
    infix fun shr(other: Int): U

    // Kotlin Int Operations
    infix fun and(other: Long): U
    infix fun or(other: Long): U
    infix fun xor(other: Long): U

}

