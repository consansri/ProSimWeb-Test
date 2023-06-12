package extendable.components.connected

import extendable.ArchConst
import extendable.components.types.Address
import kotlinx.js.BigInt

class Register(val address: Address, val name: String, private var value: Long, val description: String, val widthBit: Int) {

    init {
        require(widthBit in 1..128) {
            "Bit width must be between 1 and 128"
        }
    }

    fun setValue(newValue: Long) {
        val maxValue = (1L shl widthBit) - 1
        if (newValue !in 0..maxValue) {
            console.warn("Register.setValue(${newValue}) value must be between 0 and ${maxValue}")
            if (newValue > maxValue) {
                value = maxValue
            } else {
                value = 0
            }
        } else {
            value = newValue
        }


    }

    fun setHexValue(hexValue: String) {
        val trimmedHexValue = hexValue.trim().removePrefix(ArchConst.PRESTRING_HEX)
        val paddedHexValue = trimmedHexValue.padStart(widthBit / 4, '0')
        val truncatedHexValue = paddedHexValue.takeLast(widthBit / 4)

        if (!truncatedHexValue.matches(Regex("[0-9a-fA-F]{${widthBit / 4}}"))) {
            console.log("Register $name: Invalid Hex Input!")
            return
        }

        val newValue = truncatedHexValue.toLong(16)
        setValue(newValue)
    }

    fun incValue() {
        setValue(value + 1)
    }

    fun clear(){
        setValue(0)
    }

    fun getValue(): Long {
        return value
    }

    fun getHexValue(): String {
        val hexString = value.toString(16).padStart(widthBit / 4, '0')
        return ArchConst.PRESTRING_HEX + hexString.uppercase()
    }

    fun getBinaryValue(): String {
        var binaryString = value.toString(2).padStart(widthBit, '0')
        return ArchConst.PRESTRING_BINARY + binaryString
    }


}