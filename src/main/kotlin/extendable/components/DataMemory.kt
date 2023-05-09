package extendable.components

import kotlin.math.pow

class DataMemory {
    val addressLength: Int // in Bit
    val globalSize: Int // in Byte
    val wordLength: Int // in Byte
    val initNumber: Byte = 0

    lateinit var memArray: Array<Array<Byte>>

    constructor() {
        this.addressLength = 4
        this.wordLength = 4
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        setup()
    }

    constructor(addressLength: Int, wordLength: Int) {
        this.addressLength = addressLength
        this.wordLength = wordLength
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        setup()
    }

    private fun setup() {
        //memArray = Array(globalSize / wordLength) { Array(wordLength) { initNumber } } // !! TO BIG
    }

    public fun getAddressMax(): Int {
        return globalSize - 1
    }

    public fun getAddressMin(): Int {
        return 0
    }

    public fun save(address: Int, value: Byte) {
        memArray[address / wordLength][address % wordLength] = value
    }

    public fun loadByte(address: Int): Byte {
        return memArray[address / wordLength][address % wordLength]
    }

    public fun loadWord(address: Int): Array<Byte> {
        return memArray[address / wordLength]
    }

}