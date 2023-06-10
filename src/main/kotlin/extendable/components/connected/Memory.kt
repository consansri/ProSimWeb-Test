package extendable.components.connected

import extendable.ArchConst
import extendable.components.types.Address
import tools.HTMLTools
import tools.TypeTools
import kotlin.math.pow

class Memory {
    private val addressWidthBit: Int // in Bit
    private val globalSize: Double // in Byte
    private val wordWidthBit: Int // in Bit

    private var memList: MutableList<DMemInstance>

    constructor() {
        this.addressWidthBit = 16
        this.wordWidthBit = 8
        this.globalSize = 2.0.pow(addressWidthBit.toDouble())
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    constructor(addressWidthBit: Int, wordWidthBit: Int) {
        this.addressWidthBit = addressWidthBit
        this.wordWidthBit = wordWidthBit
        this.globalSize = 2.0.pow(addressWidthBit.toDouble())
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    private fun setup() {
        save(0.0, 0)
        save(getAddressMax(), 0)
    }

    fun save(address: Double, value: Int): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.setValue(value)
                return true
            }
        }
        memList += DMemInstance(address, value, wordWidthBit)
        return false
    }

    fun load(address: Double): Int? {
        for (instance in memList) {
            if (instance.address == address) {
                return instance.getValue()
            }
        }
        return null
    }

    fun clear() {
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    fun getMemList(): List<DMemInstance> {
        return memList
    }

    fun getAddressMax(): Double {
        return globalSize - 1
    }

    fun getWordHexString(value: Int): String {
        return TypeTools.getHexString(value.toLong(), wordWidthBit / 4)
    }

    fun getAddressHexString(address: Double): String {
        return TypeTools.getHexString(address.toLong(), addressWidthBit / 4)
    }

    class DMemInstance(val address: Double, private var value: Int, val bitWidth: Int) {

        fun setValue(newValue: Int) {
            value = newValue
        }

        fun getValue(): Int {
            return value
        }

    }


}