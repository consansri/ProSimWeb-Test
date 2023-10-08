package emulator.kit.types

import emulator.kit.Settings
import debug.DebugTools

/**
 * This Object contains all performant relevant binary calculations based on [String] representations.
 */
object BinaryTools {
    fun negotiate(aBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        var result = ""

        if (a[0] == '1') {
            result = add(inv(a), "1")
        } else {
            result = add(inv(a), "1")
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.info("BinaryTools: negotiate($aBin) -> $result")
        }

        return checkEmpty(result)
    }
    fun add(aBin: String, bBin: String): String {
        return addWithCarry(aBin, bBin).result
    }
    fun addWithCarry(aBin: String, bBin: String): AdditionResult {

        /*
         * (checked)
         */

        var carry = 0
        var result = ""

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)

        for (i in 0 until maxLength) {
            val digitA = if (i < a.length) a[a.length - 1 - i].toString().toInt() else 0
            val digitB = if (i < b.length) b[b.length - 1 - i].toString().toInt() else 0

            val sum = digitA + digitB + carry
            val digitResult = sum % 2
            carry = sum / 2

            result = digitResult.toString() + result
        }

        val carryChar: Char

        if (carry == 1) {
            carryChar = '1'
        } else {
            carryChar = '0'
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.info("BinaryTools: addWithCarry($aBin, $bBin) -> result: ${result.trimStart('0')}, carry: $carryChar")
        }

        return AdditionResult(checkEmpty(result.trimStart('0')), carryChar)
    }
    fun sub(aBin: String, bBin: String): String {

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)
        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')

        var borrow = '0'
        var result = ""

        for (i in maxLength - 1 downTo 0) {
            val digitA = paddedA[i]
            val digitB = paddedB[i]

            val diff = when {
                digitA == '0' && digitB == '0' && borrow == '0' -> {
                    borrow = '0'
                    '0'
                }
                digitA == '1' && digitB == '0' && borrow == '0' -> {
                    borrow = '0'
                    '1'
                }
                digitA == '1' && digitB == '1' && borrow == '0' -> {
                    borrow = '0'
                    '0'
                }
                digitA == '0' && digitB == '1' && borrow == '0' -> {
                    borrow = '1'
                    '1'
                }
                digitA == '0' && digitB == '0' && borrow == '1' -> {
                    borrow = '1'
                    '1'
                }
                digitA == '1' && digitB == '0' && borrow == '1' -> {
                    borrow = '0'
                    '0'
                }
                digitA == '1' && digitB == '1' && borrow == '1' -> {
                    borrow = '1'
                    '1'
                }
                digitA == '0' && digitB == '1' && borrow == '1' -> {
                    borrow = '1'
                    '0'
                }
                else -> {
                    console.error("$digitA or $digitB is not a '1' or '0'!")
                    '0'
                }
            }

            result =  diff.toString() + result
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.info("BinaryTools: sub($a, $b) -> result: ${result.trimStart('0')} should be: ${(a.toLong(2) - b.toLong(2)).toString(2)}")
        }
        return checkEmpty(result.trimStart('0'))
    }
    fun multiply(aBin: String, bBin: String): String {

        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        var result = "0"
        var aPuffer = a

        for (digit in b.reversed()) {
            try {
                if (digit == '1') {
                    val additionResult = addWithCarry(result, aPuffer)
                    if (additionResult.carry == '1') {
                        result = additionResult.carry + additionResult.result
                    } else {
                        result = additionResult.result
                    }
                }
                val aPair = addWithCarry(aPuffer, aPuffer)
                aPuffer = aPair.carry + aPair.result
            } catch (e: NumberFormatException) {
                console.warn("BinaryTools.multiply(): ${digit} is not a digit!")
            }
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.info("BinaryTools: multiply($aBin, $bBin) -> ${result.trimStart('0')}")
        }

        return checkEmpty(result.trimStart('0'))
    }
    fun divide(dividend: String, divisor: String): DivisionResult {
        val dend = dividend.trim().removePrefix(Settings.PRESTRING_BINARY).trimStart('0')
        val sor = divisor.trim().removePrefix(Settings.PRESTRING_BINARY).trimStart('0')

        if (isEqual(sor, "0")) {
            console.warn("BinaryTools.divide(): No Division Possible! divisor is zero! returning 0b0")
            return DivisionResult("0", "")
        }

        var comparison = ""
        var result = ""
        var remainingDividend = dend

        while (remainingDividend.isNotEmpty()) {
            comparison += remainingDividend[0]
            remainingDividend = remainingDividend.substring(1)

            if (isGreaterEqualThan(comparison, sor)) {
                result += "1"
                comparison = sub(comparison, sor).trimStart('0')
            } else {
                result += "0"
            }
        }
        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.log("BinaryTools: divide($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: ${comparison + remainingDividend}")
        }

        return DivisionResult(checkEmpty(result.trimStart('0')), (comparison + remainingDividend).trimStart('0'))
    }
    fun inv(aBin: String): String {

        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        var result = ""

        for (i in a.indices.reversed()) {
            if (a[i] == '1') {
                result = '0' + result
            } else {
                result = '1' + result
            }
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            console.info("BinaryTools: inv($aBin) -> $result")
        }
        return checkEmpty(result)
    }
    fun xor(aBin: String, bBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)

        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')

        var result = ""
        for (id in paddedA.indices) {
            if (paddedA[id] != paddedB[id]) {
                result += "1"
            } else {
                result += "0"
            }
        }

        return result
    }
    fun or(aBin: String, bBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)

        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')

        var result = ""
        for (id in paddedA.indices) {
            if (paddedA[id] == '1' || paddedB[id] == '1') {
                result += "1"
            } else {
                result += "0"
            }
        }

        return result
    }
    fun and(aBin: String, bBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)

        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')

        var result = ""
        for (id in paddedA.indices) {
            if (paddedA[id] == '1' && paddedB[id] == '1') {
                result += "1"
            } else {
                result += "0"
            }
        }

        return result
    }
    fun isGreaterThan(aBin: String, bBin: String): Boolean {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val maxLength = maxOf(a.length, b.length)
        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')
        for (i in paddedA.indices) {

            val aDigit = paddedA[i]
            val bDigit = paddedB[i]

            if (aDigit != bDigit) {
                if (aDigit == '1') {
                    return true
                } else {
                    return false
                }
            }
        }
        return false
    }
    fun isEqual(aBin: String, bBin: String): Boolean {

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val maxLength = maxOf(a.length, b.length)
        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')

        for (i in paddedA.indices) {
            if (paddedA[i] != paddedB[i]) {
                return false
            }
        }
        return true
    }
    fun isGreaterEqualThan(aBin: String, bBin: String): Boolean {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val maxLength = maxOf(a.length, b.length)
        val paddedA = a.padStart(maxLength, '0')
        val paddedB = b.padStart(maxLength, '0')
        for (i in paddedA.indices) {
            val aDigit = paddedA[i]
            val bDigit = paddedB[i]

            if (aDigit != bDigit) {
                if (aDigit == '1') {
                    return true
                } else {
                    return false
                }
            }
        }
        return true
    }
    fun checkEmpty(aBin: String): String {
        return if (aBin == "") {
            "0"
        } else {
            aBin
        }
    }

    data class AdditionResult(val result: String, val carry: Char)
    data class DivisionResult(val result: String, val rest: String)
}