package tools

import extendable.ArchConst

object TypeTools {
    fun getHexString(value: Long, width: Int): String {
        val hexString = value.toString(16).uppercase().padStart(width, '0')
        return ArchConst.PRESTRING_HEX + hexString
    }

    fun getASCIIFromHexString(hexString: String): String {
        val stringBuilder = StringBuilder()

        val trimmedHex = hexString.trim().removePrefix(ArchConst.PRESTRING_HEX)

        for (i in trimmedHex.indices step 2) {
            val hex = trimmedHex.substring(i, i + 2)
            val decimal = hex.toIntOrNull(16)

            if ((decimal != null) && (decimal in (32..126))) {
                stringBuilder.append(decimal.toChar())
            } else {
                stringBuilder.append("·")
            }
        }
        return stringBuilder.toString()
    }

}