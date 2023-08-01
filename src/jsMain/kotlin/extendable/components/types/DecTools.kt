package extendable.components.types

import extendable.ArchConst
import tools.DebugTools


object DecTools {

    val binaryWeights: List<BinaryWeight> = List(MutVal.Size.Bit128().bitWidth) { BinaryWeight(it, pow("2", it.toString())) }

    fun add(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        val aAbs = abs(aTrimmed)
        val bAbs = abs(bTrimmed)

        val result: String

        if (aNeg == bNeg) {
            val resultAbs = addUnsigned(aAbs, bAbs)
            result = if (aNeg) negotiate(resultAbs) else resultAbs
        } else {
            val isAAbsGreater = isGreaterEqualThan(aAbs, bAbs)
            val resultAbs = if (isAAbsGreater) subUnsigned(aAbs, bAbs) else subUnsigned(bAbs, aAbs)

            val aSign = if (aNeg) "-" else ""
            val bSign = if (bNeg) "-" else ""

            result = if (isAAbsGreater) "$aSign$resultAbs" else "$bSign$resultAbs"
        }
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.info("DecTools: ${a} + ${b} = $result")
        }
        return checkEmpty(result)
    }

    fun sub(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val result = add(aTrimmed, negotiate(bTrimmed))
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.info("DecTools: ${a} - ${b} = $result")
        }
        return checkEmpty(result)
    }

    fun multiply(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val result: String

        if (isNegative(aTrimmed) xor isNegative(bTrimmed)) {
            result = negotiate(multiplyUnsigned(abs(aTrimmed), abs(bTrimmed)))
        } else {
            result = multiplyUnsigned(abs(aTrimmed), abs(bTrimmed))
        }
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.info("DecTools: ${a} * ${b} = $result")
        }
        return checkEmpty(result)
    }

    fun divide(dividend: String, divisor: String): DivisionResult {
        val dividendTrimmed = dividend.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val divisorTrimmed = divisor.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val result: DivisionResult

        if (isNegative(dividendTrimmed) xor isNegative(divisorTrimmed)) {
            val smallResult = divideUnsigned(abs(dividendTrimmed), abs(divisorTrimmed))
            val negresult = DivisionResult(negotiate(smallResult.result), smallResult.rest)
            result = negresult
        } else {
            result = divideUnsigned(abs(dividendTrimmed), abs(divisorTrimmed))
        }
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.info("DecTools: ${dividend} / ${divisor} = $result")
        }
        return checkEmpty(result)
    }

    fun pow(base: String, exponent: String): String {
        val baseTrimmed = base.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val exponentTrimmed = exponent.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val absBase = abs(baseTrimmed)
        val absExponent = abs(exponentTrimmed)

        if (isNegative(exponentTrimmed)) {
            console.warn("DecTools.pow(): exponent = ${exponent} must be greater equal 0! (using ${absExponent} for calculation!)")
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

        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: $base^($exponent) -> result: ${result}")
        }

        return checkEmpty(result)
    }

    fun negotiate(a: String): String {
        /*
         * (checked)
         */
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val negRegex = Regex("-[0-9]+")
        val posRegex = Regex("[0-9]+")
        val zeroRegex = Regex("(-)?0+")
        if (zeroRegex.matches(aTrimmed)) {
            return aTrimmed.replace("-", "")
        }

        val result = if (negRegex.matches(aTrimmed)) {
            aTrimmed.replace("-", "")
        } else if (posRegex.matches(aTrimmed)) {
            "-$aTrimmed"
        } else {
            throw Exception("DecTools.negotiate(): ${a} is invalid!")
        }
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: negotiate($a) -> ${result}")
        }
        return checkEmpty(result)
    }

    fun isNegative(a: String): Boolean {
        /*
         * (checked)
         */
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val negRegex = Regex("-[0-9]+")
        val posRegex = Regex("[0-9]+")

        val result = if (negRegex.matches(aTrimmed) && !isEqual(abs(aTrimmed), "0")) {
            true
        } else {
            false
        }
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: isNegative($a) -> ${result}")
        }

        return result
    }

    fun isEqual(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        var aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        var bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        val result: Boolean

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

    fun isZero(a: String): Boolean {
        val result = isEqual(a, "0")
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: isZero($a) -> ${result}")
        }
        return result
    }

    fun isGreaterThan(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        val result = (isGreaterEqualThan(a, b) && !isEqual(a, b))
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: isGreaterThan($a, $b) -> $result")
        }
        return result
    }

    fun isGreaterEqualThan(a: String, b: String): Boolean {
        /*
         * (checked)
         */
        var aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        var bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val aNeg = isNegative(aTrimmed)
        val bNeg = isNegative(bTrimmed)

        if (aNeg xor bNeg) {
            return !aNeg
        } else {
            var negativeComparison: Boolean = false
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
                    result = if (negativeComparison) false else true
                } else if (aDigit < bDigit) {
                    result = if (negativeComparison) true else false
                }
                result?.let {
                    // console.warn("${aDigit},${bDigit} -> neg:${negativeComparison} result:${result}")
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
        val result = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL).replace("-", "")
        if (DebugTools.ARCH_showBVDecToolsCalculations) {
            console.log("DecTools: abs($a) -> ${result}")
        }
        return result
    }

    private fun addUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
        val paddedA = aTrimmed.padStart(maxLength, '0')
        val paddedB = bTrimmed.padStart(maxLength, '0')

        val sum = StringBuilder()
        var carry = 0

        for (i in maxLength - 1 downTo 0) {
            val digitA = paddedA[i].digitToInt()
            val digitB = paddedB[i].digitToInt()

            val digitSum = digitA + digitB + carry
            carry = digitSum / 10

            sum.insert(0, (digitSum % 10).toString())
        }

        if (carry > 0) {
            sum.insert(0, carry.toString())
        }
        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: addUnsigned($a, $b) -> $sum")
        }

        return sum.toString()
    }

    private fun subUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        val maxLength = maxOf(aTrimmed.length, bTrimmed.length)
        var paddedA = aTrimmed.padStart(maxLength, '0')
        var paddedB = bTrimmed.padStart(maxLength, '0')

        // Prevent Result from being negative
        if (!isGreaterEqualThan(paddedA, paddedB)) {
            val buffer = paddedA
            paddedA = paddedB
            paddedB = buffer
            console.warn("DecTools.subUnsigned(): a (${aTrimmed}) was smaller than b (${bTrimmed}) so they where switched up!")
        }

        val difference = StringBuilder()
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

            difference.insert(0, digitDifference.toString())
        }

        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: subUnsigned($a, $b) -> $difference")
        }

        return difference.toString().trimStart('0')
    }

    private fun multiplyUnsigned(a: String, b: String): String {
        val aTrimmed = a.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val bTrimmed = b.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        var result = "0"

        for (i in bTrimmed.length - 1 downTo 0) {
            val digitB = bTrimmed[i].digitToInt()
            var carry = 0
            val product = StringBuilder()

            for (j in aTrimmed.length - 1 downTo 0) {
                val digitA = aTrimmed[j].digitToInt()
                val digitProduct = digitA * digitB + carry
                carry = digitProduct / 10

                product.insert(0, (digitProduct % 10).toString())
            }

            if (carry > 0) {
                product.insert(0, carry.toString())
            }

            product.append("0".repeat(bTrimmed.length - i - 1))
            result = addUnsigned(result, product.toString())
        }

        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: multiplyUnsigned($a, $b) -> ${result.trimStart('0')}")
        }

        return result.trimStart('0')
    }

    private fun divideUnsigned(dividend: String, divisor: String): DivisionResult {

        val dividendTrimmed = dividend.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val divisorTrimmed = divisor.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        if (isNegative(dividendTrimmed) || isNegative(divisorTrimmed)) {
            throw Exception("DecTools.divideUnsigned(): ${dividendTrimmed}, ${divisor} not both positiv!")
        }

        if (isGreaterThan(divisorTrimmed, dividendTrimmed)) {
            console.warn("No Division possible!")
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

            val smallDivi: String
            if (needBiggerCompare != null) {
                smallDivi = dividendBuffer.substring(0, needBiggerCompare)
            } else {
                smallDivi = dividendBuffer.substring(0, comparisonLength)
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
            DebugTools.ARCH_BVDivisionLoopLimit?.let {
                if (loop < DebugTools.ARCH_BVDivisionLoopLimit) {
                    if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
                        console.log("DecTools.divideUnsigned.loop$loop: dividend: ${dividendBuffer}, result: ${result}, rest: ${rest}, smallResult: ${smallResult}")
                    }
                    loop++
                } else {
                    console.warn("DecTools: Division took to long!")
                    return DivisionResult(result.trimStart('0'), rest)
                }
            }

        }

        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: divideUnsigned($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: $rest")
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

            DebugTools.ARCH_BVDivisionLoopLimit?.let {
                if (loop < DebugTools.ARCH_BVDivisionLoopLimit) {

                    loop++
                } else {
                    console.warn("DecTools: Division took to long!")
                    return DivisionResult(result.trimStart('0'), dividendTemp)
                }
            }
        }
        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: loops needed: $loop, smallDivUnsigned($dividend, $divisor) -> result: ${result.trimStart('0')}, remainder: ${dividendTemp}")
        }
        return DivisionResult(result, dividendTemp)
    }

    private fun powUnsigned(base: String, exponent: String): String {
        /*
         * (checked)
         */
        val baseTrimmed = base.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)
        val exponentTrimmed = exponent.trim().removePrefix(ArchConst.PRESTRING_DECIMAL)

        var result = "1"

        val expoInt = exponentTrimmed.toInt()
        if (expoInt < 0) {
            throw Exception("DecTools.powUnsigned(): exponent = ${exponent} must be greater equal 0! (returning 1)")
        } else {
            for (i in 1..expoInt) {
                result = multiplyUnsigned(result, baseTrimmed)
            }
        }

        if (DebugTools.ARCH_showBVDecToolsCalculationDetails) {
            console.log("DecTools: powUnsigned($base, $exponent) -> ${result.trimStart('0')}")
        }

        return result.trimStart('0')
    }

    fun checkEmpty(a: String): String {
        return if (a.equals("")) {
            "0"
        } else {
            a
        }
    }

    fun checkEmpty(divResult: DivisionResult): DivisionResult {
        return if (divResult.result.equals("")) {
            DivisionResult("0", divResult.rest)
        } else {
            divResult
        }

    }


    data class DivisionResult(val result: String, val rest: String)
    data class BinaryWeight(val binaryIndex: Int, val weight: String)

}