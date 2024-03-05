package emulator.kit.types

import Settings
import debug.DebugTools

/**
 * This Object contains all performant relevant binary calculations based on [String] representations.
 */
object BinaryTools {

    val hexToBinDigit = mapOf(
        '0' to "0000", '1' to "0001", '2' to "0010", '3' to "0011",
        '4' to "0100", '5' to "0101", '6' to "0110", '7' to "0111",
        '8' to "1000", '9' to "1001", 'A' to "1010", 'B' to "1011",
        'C' to "1100", 'D' to "1101", 'E' to "1110", 'F' to "1111"
    )
    val binToHexDigit = hexToBinDigit.map { it.value to it.key }.toMap()

    fun negotiate(aBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val result: String = if (a[0] == '1') {
            add(inv(a), "1")
        } else {
            add(inv(a), "1")
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            CommonConsole.info("BinaryTools: negotiate($aBin) -> $result")
        }

        return checkEmpty(result)
    }

    fun add(aBin: String, bBin: String): String = addWithCarry(aBin, bBin).result

    fun addWithCarry(aBin: String, bBin: String): AdditionResult {

        /*
         * (checked)
         */

        var carry = 0
        var result = ""

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val maxLength = maxOf(a.length, b.length)

        for (i in 0..<maxLength) {
            val digitA = if (i < a.length) a[a.length - 1 - i].toString().toInt() else 0
            val digitB = if (i < b.length) b[b.length - 1 - i].toString().toInt() else 0

            val sum = digitA + digitB + carry
            val digitResult = sum % 2
            carry = sum / 2

            result = digitResult.toString() + result
        }

        val carryChar: Char = if (carry == 1) {
            '1'
        } else {
            '0'
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            CommonConsole.info("BinaryTools: addWithCarry($aBin, $bBin) -> result: ${result.trimStart('0')}, carry: $carryChar")
        }

        return AdditionResult(checkEmpty(result.trimStart('0')), carryChar)
    }

    fun sub(aBin: String, bBin: String): String = subWithBorrow(aBin, bBin).result

    fun subWithBorrow(aBin: String, bBin: String): SubtractionResult {
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
                    CommonConsole.error("$digitA or $digitB is not a '1' or '0'!")
                    '0'
                }
            }

            result = diff.toString() + result
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            CommonConsole.info("BinaryTools: sub($a, $b) -> result: ${result.trimStart('0')} should be: ${(a.toLong(2) - b.toLong(2)).toString(2)}")
        }
        return SubtractionResult(checkEmpty(result.trimStart('0')), borrow)
    }

    fun multiply(aBin: String, bBin: String): String {

        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        val aPadded = "0".repeat(b.length) + a
        val bPadded = "0".repeat(a.length) + b

        var result = "0"
        var aPuffer = aPadded

        for (digit in bPadded.reversed()) {
            try {
                if (digit == '1') {
                    val additionResult = addWithCarry(result, aPuffer)
                    result = if (additionResult.carry == '1') {
                        additionResult.carry + additionResult.result
                    } else {
                        additionResult.result
                    }
                }
                val aPair = addWithCarry(aPuffer, aPuffer)
                aPuffer = aPair.carry + aPair.result
            } catch (e: NumberFormatException) {
                CommonConsole.warn("BinaryTools.multiply(): $digit is not a digit!")
            }
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            CommonConsole.info("BinaryTools: multiply($aBin, $bBin) -> ${result.trimStart('0')}")
        }

        return checkEmpty(result.trimStart('0'))
    }

    fun multiplySigned(aBin: String, bBin: String): String {
        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        return if (a.isNotEmpty() && b.isNotEmpty()) {
            val aSign = a[0]
            val bSign = b[0]

            val aAbs = if (aSign == '1') negotiate(a.substring(1)) else a.substring(1)
            val bAbs = if (bSign == '1') negotiate(b.substring(1)) else b.substring(1)

            val unsignedResult = multiply(aAbs, bAbs)
            val resultSign = if (aSign == bSign || !unsignedResult.contains('1')) '0' else '1'
            if (resultSign == '1') negotiate("0$unsignedResult") else "0$unsignedResult"
        } else {
            CommonConsole.warn("BinaryTools: multiplication factor is empty!")
            "0"
        }
    }

    fun multiplyMixed(aSigned: String, bUnsigned: String): String {
        val a = aSigned.trim().removePrefix(Settings.PRESTRING_BINARY)
        val b = bUnsigned.trim().removePrefix(Settings.PRESTRING_BINARY)

        return if (a.isNotEmpty() && b.isNotEmpty()) {
            val aSign = a[0]

            val aAbs = if (aSign == '1') negotiate(a.substring(1)) else a.substring(1)

            val unsignedResult = multiply(aAbs, b)
            val resultSign = if (aSign == '0' || !unsignedResult.contains('1')) '0' else '1'
            if (resultSign == '1') negotiate("0$unsignedResult") else "0$unsignedResult"
        } else {
            CommonConsole.warn("BinaryTools: multiplication factor is empty!")
            "0"
        }
    }

    fun divide(dividend: String, divisor: String): DivisionResult {
        val dend = dividend.trim().removePrefix(Settings.PRESTRING_BINARY).trimStart('0')
        val sor = divisor.trim().removePrefix(Settings.PRESTRING_BINARY).trimStart('0')

        if (isEqual(sor, "0")) {
            CommonConsole.warn("BinaryTools.divide(): No Division Possible! divisor is zero! returning 0b0")
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
            println("BinaryTools: divide($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: ${comparison + remainingDividend}")
        }

        return DivisionResult(checkEmpty(result.trimStart('0')), (comparison + remainingDividend).trimStart('0'))
    }

    fun divideSigned(dividend: String, divisor: String): DivisionResult {
        val dend = dividend.trim().removePrefix(Settings.PRESTRING_BINARY)
        val sor = divisor.trim().removePrefix(Settings.PRESTRING_BINARY)

        if (dend.isEmpty() || sor.isEmpty()) {
            CommonConsole.warn("BinaryTools: Dividend or Divisor are empty!")
            return DivisionResult("0", "")
        }
        val dendSign = dend[0]
        val sorSign = sor[0]

        val uDend = if (dendSign == '1') negotiate(dend.substring(1)) else dend.substring(1)
        val uSor = if (sorSign == '1') negotiate(sor.substring(1)) else sor.substring(1)

        val uResult = divide(uDend, uSor)

        val resultSign = if (dendSign == sorSign) '0' else '1'

        val quotient = if (resultSign == '1') negotiate("0" + uResult.result) else "0" + uResult.result
        val remainder = "0" + uResult.remainder
        return DivisionResult(quotient, remainder)
    }

    fun divideMixed(uDividend: String, sDivisor: String): DivisionResult {
        val dend = uDividend.trim().removePrefix(Settings.PRESTRING_BINARY)
        val sor = sDivisor.trim().removePrefix(Settings.PRESTRING_BINARY)

        if (dend.isEmpty() || sor.isEmpty()) {
            CommonConsole.warn("BinaryTools: Dividend or Divisor are empty!")
            return DivisionResult("0", "")
        }

        val sorSign = sor[0]

        val uSor = if (sorSign == '1') negotiate(sor.substring(1)) else sor.substring(1)

        val uResult = divide(dend, uSor)

        val quotient = if (sorSign == '1') negotiate("0" + uResult.result) else "0" + uResult.result
        val remainder = "0" + uResult.remainder
        return DivisionResult(quotient, remainder)
    }

    fun inv(aBin: String): String {

        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(Settings.PRESTRING_BINARY)

        var result = ""

        for (i in a.indices.reversed()) {
            result = if (a[i] == '1') {
                "0$result"
            } else {
                "1$result"
            }
        }

        if (DebugTools.KIT_showValBinaryToolsCalculations) {
            CommonConsole.info("BinaryTools: inv($aBin) -> $result")
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
            result += if (paddedA[id] != paddedB[id]) {
                "1"
            } else {
                "0"
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
            result += if (paddedA[id] == '1' || paddedB[id] == '1') {
                "1"
            } else {
                "0"
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
            result += if (paddedA[id] == '1' && paddedB[id] == '1') {
                "1"
            } else {
                "0"
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
                return aDigit == '1'
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
                return aDigit == '1'
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
    data class SubtractionResult(val result: String, val borrow: Char)
    data class DivisionResult(val result: String, val remainder: String)
}