package emulator.kit.types

import Settings
import debug.DebugTools

/**
 * This Object contains all performant relevant decimal calculations based on [String] representations.
 */
object DecTools {

    private val negRegex = Regex("-[0-9]+")
    private val posRegex = Regex("[0-9]+")
    private val zeroRegex = Regex("(-)?0+")
    val binaryWeights: List<BinaryWeight> = List(Variable.Size.Bit128().bitWidth) { BinaryWeight(it, pow("2", it.toString())) }

    fun add(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        val aAbs = abs(aTrimmed)
        val bAbs = abs(bTrimmed)

        val result: String = if (aNeg == bNeg) {
            val resultAbs = addUnsigned(aAbs, bAbs)
            if (aNeg) negotiate(resultAbs) else resultAbs
        } else {
            val isAAbsGreater = isGreaterEqualThan(aAbs, bAbs)
            val resultAbs = if (isAAbsGreater) subUnsigned(aAbs, bAbs) else subUnsigned(bAbs, aAbs)

            val aSign = if (aNeg) "-" else ""
            val bSign = if (bNeg) "-" else ""

            if (isAAbsGreater) "$aSign$resultAbs" else "$bSign$resultAbs"
        }
        if (DebugTools.KIT_showValDecToolsCalculations) {
            CommonConsole.info("DecTools: $a + $b = $result")
        }
        return checkEmpty(result)
    }

    fun sub(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val result = add(aTrimmed, negotiate(bTrimmed))
        if (DebugTools.KIT_showValDecToolsCalculations) {
            CommonConsole.info("DecTools: $a - $b = $result")
        }
        return checkEmpty(result)
    }

    fun multiply(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val result: String = if (isNegative(aTrimmed) xor isNegative(bTrimmed)) {
            negotiate(multiplyUnsigned(abs(aTrimmed), abs(bTrimmed)))
        } else {
            multiplyUnsigned(abs(aTrimmed), abs(bTrimmed))
        }
        if (DebugTools.KIT_showValDecToolsCalculations) {
            CommonConsole.info("DecTools: $a * $b = $result")
        }
        return checkEmpty(result)
    }

    fun divide(dividend: String, divisor: String): DivisionResult {
        val dividendTrimmed = dividend.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val divisorTrimmed = divisor.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val result: DivisionResult = if (isNegative(dividendTrimmed) xor isNegative(divisorTrimmed)) {
            val smallResult = divideUnsigned(abs(dividendTrimmed), abs(divisorTrimmed))
            val negresult = DivisionResult(negotiate(smallResult.result), smallResult.rest)
            negresult
        } else {
            divideUnsigned(abs(dividendTrimmed), abs(divisorTrimmed))
        }
        if (DebugTools.KIT_showValDecToolsCalculations) {
            CommonConsole.info("DecTools: $dividend / $divisor = $result")
        }
        return checkEmpty(result)
    }

    fun pow(base: String, exponent: String): String {
        val baseTrimmed = base.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val exponentTrimmed = exponent.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val absBase = abs(baseTrimmed)
        val absExponent = abs(exponentTrimmed)

        if (isNegative(exponentTrimmed)) {
            CommonConsole.warn("DecTools.pow(): exponent = $exponent must be greater equal 0! (using $absExponent for calculation!)")
        }

        val result = if (isNegative(baseTrimmed)) {
            if (isZero(divideUnsigned(abs(exponentTrimmed), "2").rest)) {
                powUnsigned(absBase, absExponent)
            } else {
                negotiate(powUnsigned(absBase, absExponent))
            }
        } else {
            powUnsigned(absBase, absExponent)
        }

        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: $base^($exponent) -> result: $result")
        }

        return checkEmpty(result)
    }

    fun negotiate(a: String): String {
        /*
         * (checked)
         */
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        if (zeroRegex.matches(aTrimmed)) {
            return aTrimmed.replace("-", "")
        }

        val result = if (negRegex.matches(aTrimmed)) {
            aTrimmed.replace("-", "")
        } else if (posRegex.matches(aTrimmed)) {
            "-$aTrimmed"
        } else {
            throw Exception("DecTools.negotiate(): $a is invalid!")
        }
        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: negotiate($a) -> $result")
        }
        return checkEmpty(result)
    }

    fun isNegative(a: String): Boolean {
        /*
         * (checked)
         */
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val result = negRegex.matches(aTrimmed) && !isEqual(abs(aTrimmed), "0")
        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: isNegative($a) -> $result")
        }

        return result
    }

    fun isEqual(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        var aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        var bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        if (aNeg xor bNeg) {
            return false
        } else {
            if (aNeg && bNeg) {
                aTrimmed = negotiate(aTrimmed)
                bTrimmed = negotiate(bTrimmed)
            }

            val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
            aTrimmed = aTrimmed.padStart(maxLength, '0')
            bTrimmed = bTrimmed.padStart(maxLength, '0')

            for (i in aTrimmed.indices) {
                val aDigit = aTrimmed[i].digitToInt()
                val bDigit = bTrimmed[i].digitToInt()

                if (aDigit != bDigit) {
                    return false
                }
            }
        }

        return true
    }

    private fun isZero(a: String): Boolean {
        val result = isEqual(a, "0")
        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: isZero($a) -> $result")
        }
        return result
    }

    fun isGreaterThan(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        val result = (isGreaterEqualThan(a, b) && !isEqual(a, b))
        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: isGreaterThan($a, $b) -> $result")
        }
        return result
    }

    fun isGreaterEqualThan(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        var aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        var bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        if (aNeg xor bNeg) {
            return !aNeg
        } else {
            var negativeComparison = false
            if (aNeg && bNeg) {
                aTrimmed = negotiate(aTrimmed)
                bTrimmed = negotiate(bTrimmed)
                negativeComparison = true
            }

            val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
            aTrimmed = aTrimmed.padStart(maxLength, '0')
            bTrimmed = bTrimmed.padStart(maxLength, '0')

            for (i in aTrimmed.indices) {
                val aDigit = aTrimmed[i].digitToInt()
                val bDigit = bTrimmed[i].digitToInt()

                var result: Boolean? = null
                if (aDigit > bDigit) {
                    result = !negativeComparison
                } else if (aDigit < bDigit) {
                    result = negativeComparison
                }
                result?.let {
                    // Console.warn("${aDigit},${bDigit} -> neg:${negativeComparison} result:${result}")
                    return result
                }
            }
            return true
        }
    }

    fun abs(a: String): String {
        /*
         * (checked)
         */
        val result = a.trim().removePrefix(Settings.PRESTRING_DECIMAL).replace("-", "")
        if (DebugTools.KIT_showValDecToolsCalculations) {
            println("DecTools: abs($a) -> $result")
        }
        return result
    }

    private fun addUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
        val paddedA = aTrimmed.padStart(maxLength, '0')
        val paddedB = bTrimmed.padStart(maxLength, '0')

        var sum = ""
        var carry = 0

        for (i in maxLength - 1 downTo 0) {
            val digitA = paddedA[i].digitToInt()
            val digitB = paddedB[i].digitToInt()

            val digitSum = digitA + digitB + carry
            carry = digitSum / 10

            sum = (digitSum % 10).toString() + sum
        }

        if (carry > 0) {
            sum = carry.toString() + sum
        }
        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: addUnsigned($a, $b) -> $sum")
        }

        return sum
    }

    private fun subUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
        var paddedA = aTrimmed.padStart(maxLength, '0')
        var paddedB = bTrimmed.padStart(maxLength, '0')

        // Prevent Result from being negative
        if (!isGreaterEqualThan(paddedA, paddedB)) {
            val buffer = paddedA
            paddedA = paddedB
            paddedB = buffer
            CommonConsole.warn("DecTools.subUnsigned(): a (${aTrimmed}) was smaller than b (${bTrimmed}) so they where switched up!")
        }

        var difference = ""
        var borrow = 0

        for (i in maxLength - 1 downTo 0) {
            val digitA = paddedA[i].digitToInt()
            val digitB = paddedB[i].digitToInt()

            var digitDifference = digitA - digitB - borrow

            if (digitDifference < 0) {
                digitDifference += 10
                borrow = 1
            } else {
                borrow = 0
            }

            difference = digitDifference.toString() + difference
        }

        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: subUnsigned($a, $b) -> $difference")
        }

        return difference.trimStart('0')
    }

    private fun multiplyUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        var result = "0"

        for (i in bTrimmed.length - 1 downTo 0) {
            val digitB = bTrimmed[i].digitToInt()
            var carry = 0
            var product = ""

            for (j in aTrimmed.length - 1 downTo 0) {
                val digitA = aTrimmed[j].digitToInt()
                val digitProduct = digitA * digitB + carry
                carry = digitProduct / 10

                product = (digitProduct % 10).toString() + product
            }

            if (carry > 0) {
                product = carry.toString() + product
            }

            product += "0".repeat(bTrimmed.length - i - 1)
            result = addUnsigned(result, product)
        }

        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: multiplyUnsigned($a, $b) -> ${result.trimStart('0')}")
        }

        return result.trimStart('0')
    }

    private fun divideUnsigned(dividend: String, divisor: String): DivisionResult {

        val dividendTrimmed = dividend.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val divisorTrimmed = divisor.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        if (isNegative(dividendTrimmed) || isNegative(divisorTrimmed)) {
            throw Exception("DecTools.divideUnsigned(): ${dividendTrimmed}, $divisor not both positiv!")
        }

        if (isGreaterThan(divisorTrimmed, dividendTrimmed)) {
            CommonConsole.warn("No Division possible!")
            return DivisionResult("0", dividendTrimmed)
        }

        if (isEqual(dividendTrimmed, divisorTrimmed)) {
            return DivisionResult("1", "0")
        }

        val comparisonLength = divisorTrimmed.length
        var needBiggerCompare: Int? = null

        var rest = "0"
        var dividendBuffer = dividendTrimmed
        var result = ""

        var loop = 0

        while (isGreaterEqualThan(dividendBuffer, divisorTrimmed)) {

            val smallDivi: String = if (needBiggerCompare != null) {
                dividendBuffer.substring(0, needBiggerCompare)
            } else {
                dividendBuffer.substring(0, comparisonLength)
            }

            val smallResult = smallDivUnsigned(smallDivi, divisorTrimmed)
            if (!isZero(smallResult.result)) {
                result += smallResult.result
            }
            rest = smallResult.rest
            if (smallResult.result == "0") {
                if (needBiggerCompare != null) {
                    needBiggerCompare++
                } else {
                    needBiggerCompare = comparisonLength + 1
                }
            } else {
                needBiggerCompare = null
            }

            dividendBuffer = dividendBuffer.removePrefix(smallDivi)
            dividendBuffer = "$rest$dividendBuffer".trimStart('0')
            if (dividendBuffer.isEmpty()) {
                dividendBuffer = "0"
            }
            DebugTools.KIT_ValBinaryToolsDivisionLoopLimit?.let {
                if (loop < DebugTools.KIT_ValBinaryToolsDivisionLoopLimit) {
                    if (DebugTools.KIT_showValDecToolsCalculationDetails) {
                        println("DecTools.divideUnsigned.loop$loop: dividend: ${dividendBuffer}, result: ${result}, rest: ${rest}, smallResult: $smallResult")
                    }
                    loop++
                } else {
                    CommonConsole.warn("DecTools: Division took to long!")
                    return DivisionResult(result.trimStart('0'), rest)
                }
            }

        }

        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: divideUnsigned($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: $rest")
        }

        return DivisionResult(result.trimStart('0'), rest)
    }

    private fun smallDivUnsigned(dividend: String, divisor: String): DivisionResult {
        var result = "0"
        var dividendTemp = dividend

        var loop = 0
        while (isGreaterEqualThan(dividendTemp, divisor)) {
            dividendTemp = subUnsigned(dividendTemp, divisor)
            result = addUnsigned(result, "1")

            DebugTools.KIT_ValBinaryToolsDivisionLoopLimit?.let {
                if (loop < DebugTools.KIT_ValBinaryToolsDivisionLoopLimit) {

                    loop++
                } else {
                    CommonConsole.warn("DecTools: Division took to long!")
                    return DivisionResult(result.trimStart('0'), dividendTemp)
                }
            }
        }
        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: loops needed: $loop, smallDivUnsigned($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: $dividendTemp")
        }
        return DivisionResult(result, dividendTemp)
    }

    private fun powUnsigned(base: String, exponent: String): String {
        /*
         * (checked)
         */
        val baseTrimmed = base.trim().removePrefix(Settings.PRESTRING_DECIMAL)
        val exponentTrimmed = exponent.trim().removePrefix(Settings.PRESTRING_DECIMAL)

        var result = "1"

        val expoInt = exponentTrimmed.toInt()
        if (expoInt < 0) {
            throw Exception("DecTools.powUnsigned(): exponent = $exponent must be greater equal 0! (returning 1)")
        } else {
            for (i in 1..expoInt) {
                result = multiplyUnsigned(result, baseTrimmed)
            }
        }

        if (DebugTools.KIT_showValDecToolsCalculationDetails) {
            println("DecTools: powUnsigned($base, $exponent) -> ${result.trimStart('0')}")
        }

        return result.trimStart('0')
    }

    fun checkEmpty(a: String): String {
        return if (a == "") {
            "0"
        } else {
            a
        }
    }

    private fun checkEmpty(divResult: DivisionResult): DivisionResult {
        return if (divResult.result == "") {
            DivisionResult("0", divResult.rest)
        } else {
            divResult
        }

    }


    data class DivisionResult(val result: String, val rest: String)
    data class BinaryWeight(val binaryIndex: Int, val weight: String)

}