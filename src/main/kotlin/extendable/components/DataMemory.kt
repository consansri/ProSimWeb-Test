package extendable.components

import kotlin.math.pow

class DataMemory {
    private val addressLength: Int // in Bit
    private val globalSize: Double // in Byte
    private val wordLength: Int // in Byte

    private var memList: MutableList<DMemInstance>

    constructor() {
        this.addressLength = 4
        this.wordLength = 4
        this.globalSize = 2.0.pow(addressLength.toDouble())
        this.memList = mutableListOf<DMemInstance>()
        setup()
    }

    constructor(addressLength: Int, wordLength: Int) {
        this.addressLength = addressLength
        this.wordLength = wordLength
        this.globalSize = 2.0.pow(addressLength.toDouble())
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

    fun getWordLength(): Int {
        return wordLength
    }

    class DMemInstance(val address: Double, var value: Int) {

    }


}