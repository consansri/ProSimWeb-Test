package tools

import extendable.ArchConst

object TypeTools {
    fun getHexString(value: Long, width: Int): String {
        val hexString = value.toString(16).uppercase().padStart(width, '0')
        return ArchConst.PRESTRING_HEX + hexString
    }

}