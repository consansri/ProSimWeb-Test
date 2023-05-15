package extendable.components

import kotlin.math.pow

class DataMemory {
    val addressLength: Int // in Bit
    val globalSize: Int // in Byte
    val wordLength: Int // in Byte

    private var memList: List<DMemInstance>

    constructor() {
        this.addressLength = 4
        this.wordLength = 4
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        this.memList = emptyList<DMemInstance>()
    }

    constructor(addressLength: Int, wordLength: Int) {
        this.addressLength = addressLength
        this.wordLength = wordLength
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        this.memList = emptyList<DMemInstance>()
    }

    fun save(address: Int, value: Int): Boolean {
        for (instance in memList) {
            if (instance.address == address) {
                instance.value = value
                return true
            }
        }
        memList += DMemInstance(address, value)
        return false
    }

    fun load(address: Int): Int? {
        for (instance in memList) {
            if (instance.address == address) {
                return instance.value
            }
        }
        return null
    }

    fun getAddressMax(): Int {
        return globalSize - 1
    }

    private class DMemInstance(val address: Int, var value: Int) {

    }


}