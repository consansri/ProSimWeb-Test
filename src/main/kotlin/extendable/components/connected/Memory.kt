package extendable.components.connected

import kotlin.math.pow

class Memory {
    private val addressWidthBit: Int // in Bit
    private val globalSize: Double // in Byte
    private val wordWidthBit: Int // in Bit

    private var memList: MutableList<DMemInstance>

    constructor() {
        this.addressWidthBit = 4
        this.wordWidthBit = 4
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

    private fun setup(){
        save(0.0, 0)
        save(getAddressMax(), 0)
    }

    fun save(address: Double, value: Int): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.value = value
                return true
            }
        }
        memList += DMemInstance(address, value)
        return false
    }

    fun load(address: Double): Int? {
        for (instance in memList) {
            if (instance.address == address) {
                return instance.value
            }
        }
        return null
    }

    fun clear(){
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    fun getMemList(): List<DMemInstance>{
        return memList
    }

    fun getAddressMax(): Double {
        return globalSize - 1
    }

    fun getWordWithBit(): Int {
        return wordWidthBit
    }

    class DMemInstance(val address: Double, var value: Int) {

    }


}