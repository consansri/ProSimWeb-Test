package cengine.util.integer

import Settings
import cengine.util.integer.binary.BinaryTools
import cengine.util.integer.decimal.DecimalTools
import debug.DebugTools
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

/**
 * Type Conversion
 */

/**
 * Returns a [Size.Bit8] zero of the Type of [Value] contained in [this].
 * Identified by Prefix
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun String.getType(): Value {
    var removedPrefString = trim().removePrefix(Settings.PRESTRING_BINARY)
    if (removedPrefString.length < trim().length - 1) {
        return Bin("0", Size.Bit8)
    }
    removedPrefString = trim().removePrefix(Settings.PRESTRING_HEX)
    if (removedPrefString.length < trim().length - 1) {
        return Hex("0", Size.Bit8)
    }
    removedPrefString = trim().removePrefix(Settings.PRESTRING_OCT)
    if (removedPrefString.length < trim().length - 1) {
        return Oct("0", Size.Bit8)
    }
    removedPrefString = trim().removePrefix(Settings.PRESTRING_UDECIMAL)
    if (removedPrefString.length < trim().length - 1) {
        return UDec("u0", Size.Bit8)
    }
    return Dec("0", Size.Bit8)
}

/**
 * Converts [Bin] to [Hex] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Bin.getHex(): Hex {
    var hexStr = ""

    var binStr = rawInput
    binStr = if (binStr.length % 4 != 0) {
        "0".repeat(4 - (binStr.length % 4)) + binStr
    } else {
        binStr
    }

    for (i in binStr.indices step 4) {
        val substring = binStr.substring(i, i + 4)
        hexStr += BinaryTools.binToHexDigit[substring] ?: break
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: ${toString()} to $hexStr")
    }

    return Hex(hexStr, size)
}

/**
 * Converts [Bin] to [Oct] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Bin.getOct(): Oct {
    var octStr = ""

    var binStr = rawInput
    binStr = if (binStr.length % 3 != 0) {
        "0".repeat(3 - (binStr.length % 3)) + binStr
    } else {
        binStr
    }

    for (i in binStr.indices step 3) {
        val substring = binStr.substring(i, i + 3)
        octStr += BinaryTools.binToOctDigit[substring] ?: break
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: ${toString()} to $octStr")
    }

    return Oct(octStr, size)
}

/**
 * Converts [Oct] to [Bin] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Oct.getBinary(): Bin {
    var binStr = ""

    val hexStr = rawInput

    for (i in hexStr.indices) {
        binStr += BinaryTools.octToBinDigit[hexStr[i]]
    }
    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: $this to $binStr")
    }
    return Bin(binStr, size)
}

/**
 * Converts [Hex] to [Bin] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Hex.getBinary(): Bin {
    var binStr = ""

    val hexStr = rawInput.uppercase()

    for (i in hexStr.indices) {
        binStr += BinaryTools.hexToBinDigit[hexStr[i]]
    }
    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: ${toString()} to $binStr")
    }
    return Bin(binStr, size)
}

/**
 * Converts [Dec] to [Bin] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Dec.getBinary(): Bin {

    var decString = rawInput

    if (isNegative()) {
        decString = DecimalTools.negotiate(decString)
        decString = DecimalTools.sub(decString, "1")
    }

    var binaryStr = ""

    for (i in size.bitWidth - 1 downTo 0) {
        val weight = DecimalTools.binaryWeights[i].weight
        if (DecimalTools.isGreaterEqualThan(decString, weight)) {
            binaryStr += "1"
            decString = DecimalTools.sub(decString, weight)
        } else {
            binaryStr += "0"
        }
    }

    if (binaryStr == "") {
        nativeWarn("Conversion.getBinary(dec: Dec) : error in calculation $rawInput to $binaryStr")
    }

    if (isNegative()) {
        binaryStr = BinaryTools.inv(binaryStr)
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: $this to $binaryStr")
    }

    return Bin(binaryStr, size)
}

/**
 * Converts [UDec] to [Bin] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun UDec.getBinary(): Bin {

    var udecString = rawInput

    var binaryStr = ""

    for (i in size.bitWidth - 1 downTo 0) {
        val weight = DecimalTools.binaryWeights[i].weight
        if (DecimalTools.isGreaterEqualThan(udecString, weight)) {
            binaryStr += "1"
            udecString = DecimalTools.sub(udecString, weight)
        } else {
            binaryStr += "0"
        }
    }

    if (binaryStr == "") {
        nativeWarn("Conversion.getBinary(udec: UDec) : error in calculation $rawInput to $binaryStr")
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: $this to $binaryStr")
    }

    return Bin(binaryStr, size)
}

/**
 * Converts [Bin] to [Dec] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Bin.getDec(): Dec {
    var binString = rawInput
    var decString = "0"
    val negative: Boolean

    // check negative/pos binary value
    if (size.bitWidth == binString.length) {
        if (binString[0] == '1') {
            negative = true
            binString = BinaryTools.negotiate(binString)
        } else {
            negative = false
        }
    } else {
        binString = binString.padStart(size.bitWidth, '0')
        negative = false
    }

    for (index in binString.indices) {
        val binWeight = binString.length - 1 - index
        val decWeight = DecimalTools.binaryWeights[binWeight].weight
        if (binString[index] == '1') {
            decString = DecimalTools.add(decString, decWeight)
        }
    }

    if (negative) {
        decString = DecimalTools.negotiate(decString)
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: ${toString()} to $decString")
    }

    return Dec(decString, size)
}

/**
 * Converts [Bin] to [UDec] representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Bin.getUDec(): UDec {
    val binString = rawInput

    var udecString = "0"
    if (binString.isNotEmpty()) {
        val absBin: String = binString

        for (index in absBin.indices) {
            val binWeight = absBin.length - 1 - index
            val decWeight = DecimalTools.binaryWeights[binWeight].weight
            // val add = DecTools.pow("2", i.toString(10))
            if (absBin[index] == '1') {
                udecString = DecimalTools.add(udecString, decWeight)
            }
        }
    }
    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: $this to $udecString")
    }

    return UDec(udecString, size)
}

/**
 * Converts [Value] to ASCII representation.
 */
@Deprecated("Use IntNumber instead, cause it's way faster then Value!")
fun Value.getASCII(): String {
    val stringBuilder = StringBuilder()

    val hexString = when (this) {
        is Hex -> rawInput
        is Oct -> toHex().rawInput
        is Bin -> toHex().rawInput
        is Dec -> toHex().rawInput
        is UDec -> toHex().rawInput
    }

    val trimmedHex = hexString.trim().removePrefix(Settings.PRESTRING_HEX)

    for (i in trimmedHex.indices step 2) {
        val hex = trimmedHex.substring(i, i + 2)
        val decimal = hex.toIntOrNull(16)

        if ((decimal != null) && (decimal in (32..126))) {
            stringBuilder.append(decimal.toChar())
        } else {
            stringBuilder.append("·")
        }
    }

    if (DebugTools.KIT_showValTypeConversionInfo) {
        nativeInfo("Conversion: ${toHex()} to $stringBuilder")
    }

    return stringBuilder.toString()
}