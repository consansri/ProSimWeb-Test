package cengine.util.integer

import emulator.kit.nativeError

/**
 * Bounds are used to check if a specific [Value] is in it's [Bounds] which are defined through it's [Size].
 * Bounds should be defined for each [Size].
 */
class Bounds {

    val min: String
    val umin: String
    val max: String
    val umax: String

    constructor(size: Size) {
        when (size) {
            is Size.Bit1 -> {
                this.min = "0"
                this.max = "1"
                this.umin = "0"
                this.umax = "1"
            }

            is Size.Bit8 -> {
                this.min = "-128"
                this.max = "127"
                this.umin = "0"
                this.umax = "255"
            }

            is Size.Bit16 -> {
                this.min = "-32768"
                this.max = "32767"
                this.umin = "0"
                this.umax = "65535"
            }

            is Size.Bit32 -> {
                this.min = "-2147483648"
                this.max = "2147483647"
                this.umin = "0"
                this.umax = "4294967295"
            }

            is Size.Bit64 -> {
                this.min = "-9223372036854775807"
                this.max = "9223372036854775807"
                this.umin = "0"
                this.umax = "18446744073709551615"
            }

            is Size.Bit128 -> {
                this.min = "-170141183460469231731687303715884105728"
                this.max = "170141183460469231731687303715884105727"
                this.umin = "0"
                this.umax = "340282366920938463463374607431768211455"
            }

            is Size.Bit2 -> {
                this.min = "-2"
                this.max = "1"
                this.umin = "0"
                this.umax = "3"
            }

            is Size.Bit3 -> {
                this.min = "-4"
                this.max = "3"
                this.umin = "0"
                this.umax = "7"
            }

            is Size.Bit4 -> {
                this.min = "-8"
                this.max = "7"
                this.umin = "0"
                this.umax = "15"
            }

            is Size.Bit5 -> {
                this.min = "-16"
                this.max = "15"
                this.umin = "0"
                this.umax = "31"
            }

            is Size.Bit6 -> {
                this.min = "-32"
                this.max = "31"
                this.umin = "0"
                this.umax = "63"
            }

            is Size.Bit7 -> {
                this.min = "-64"
                this.max = "63"
                this.umin = "0"
                this.umax = "127"
            }

            is Size.Bit9 -> {
                this.min = "-256"
                this.max = "255"
                this.umin = "0"
                this.umax = "511"
            }

            is Size.Bit12 -> {
                this.min = "-2048"
                this.max = "2047"
                this.umin = "0"
                this.umax = "4095"
            }

            is Size.Bit18 -> {
                this.min = "-131072"
                this.max = "131071"
                this.umin = "0"
                this.umax = "262143"
            }

            is Size.Bit20 -> {
                this.min = "-524288"
                this.max = "524287"
                this.umin = "0"
                this.umax = "1048575"
            }

            is Size.Bit24 -> {
                this.min = "-8388608"
                this.max = "8388607"
                this.umin = "0"
                this.umax = "16777215"
            }

            is Size.Bit26 -> {
                this.min = "-33554432"
                this.max = "33554431"
                this.umin = "0"
                this.umax = "67108864"
            }

            is Size.Bit28 -> {
                this.min = "-124217728"
                this.max = "124217727"
                this.umin = "0"
                this.umax = "268435456"
            }

            is Size.Bit40 -> {
                this.min = "-549755813888"
                this.max = "549755813887"
                this.umin = "0"
                this.umax = "1099511627776"
            }

            is Size.Bit44 -> {
                this.min = "-17592186044416"
                this.max = "17592186044415"
                this.umin = "0"
                this.umax = "17592186044415"
            }

            is Size.Bit48 -> {
                this.min = "-140737488355328"
                this.max = "140737488355327"
                this.umin = "0"
                this.umax = "281474976710655"
            }

            is Size.Bit52 -> {
                this.min = "-2251799813685248"
                this.max = "2251799813685247"
                this.umin = "0"
                this.umax = "4503599627370496"
            }

            is Size.Bit56 -> {
                this.min = "-36028797018963968"
                this.max = "36028797018963967"
                this.umin = "0"
                this.umax = "72057594037927935"
            }

            is Size.Bit60 -> {
                this.min = "-576460752303423488"
                this.max = "576460752303423487"
                this.umin = "0"
                this.umax = "1152921504606846975"
            }

            is Size.Original -> {
                nativeError("Bounds: Can't get bounds from original Size Type! Use getNearestSize() or getNearestDecSize() first!")

                this.min = "not identified"
                this.max = "not identified"
                this.umin = "0"
                this.umax = "not identified"
            }
        }
    }

    constructor(min: String, max: String) {
        this.min = min
        this.max = max
        this.umin = "0"
        this.umax = DecTools.abs(DecTools.sub(min, max))
    }
}