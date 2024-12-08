package cengine.util.newint

interface ArithOperationProvider<in T: Any, out U: Any> {

    operator fun plus(other: T): U
    operator fun minus(other: T): U
    operator fun times(other: T): U
    operator fun div(other: T): U
    operator fun rem(other: T): U

    operator fun unaryMinus(): U

    operator fun plus(other: Int): U
    operator fun minus(other: Int): U
    operator fun times(other: Int): U
    operator fun div(other: Int): U
    operator fun rem(other: Int): U

}