package extendable.components.types

import extendable.ArchConst
import tools.DebugTools

object BinaryTools {

    fun negotiate(aBin: String): String {
        val a = aBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)

        var result = ""

        if (a[0] == '1') {
            result = add(inv(a), "1")
        } else {
            result = add(inv(a), "1")
        }

        if(DebugTools.showBinaryToolsCalculations){
            console.info("BinaryTools: negotiate($aBin) -> $result")
        }

        return result
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

        val a = aBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)

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

        if(DebugTools.showBinaryToolsCalculations){
            console.info("BinaryTools: addWithCarry($aBin, $bBin) -> result: ${result.trimStart('0')}, carry: $carryChar")
        }

        return AdditionResult(result.trimStart('0'), carryChar)
    }

    fun multiply(aBin: String, bBin: String): String {

        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)
        val b = bBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)

        var result = "0"
        var aPuffer = a

        for (digit in b.reversed()) {
            try {
                if (digit == '1') {
                    val additionResult = addWithCarry(result, aPuffer)
                    if(additionResult.carry == '1'){
                        result = additionResult.carry + additionResult.result
                    }else{
                        result = additionResult.result
                    }
                }
                val aPair = addWithCarry(aPuffer, aPuffer)
                aPuffer = aPair.carry + aPair.result
            } catch (e: NumberFormatException) {
                console.warn("BinaryTools.multiply(): ${digit} is not a digit!")
            }
        }

        if(DebugTools.showBinaryToolsCalculations){
            console.info("BinaryTools: multiply($aBin, $bBin) -> ${result.trimStart('0')}")
        }

        return result.trimStart('0')
    }

    fun inv(aBin: String): String {
        /*
         * (checked)
         */

        val a = aBin.trim().removePrefix(ArchConst.PRESTRING_BINARY)

        var result = ""

        for (i in a.indices.reversed()) {
            if (a[i] == '1') {
                result = '0' + result
            } else {
                result = '1' + result
            }
        }

        if(DebugTools.showBinaryToolsCalculations){
            console.info("BinaryTools: inv($aBin) -> $result")
        }
        return result
    }

    data class AdditionResult(val result: String, val carry: Char)


}