package extendable.components

import kotlin.math.pow

class ProgramMemory {

    val addressLength: Int // in Bit
    val instructionLength: Int // in Bit
    val extensionLength: Int // in Bit
    val globalSize: Int // Amount of Values in Program Memory

    private var memList: List<PMemInstance>

    constructor() {
        this.addressLength = 16 // Bit
        this.instructionLength = 32 // Bit
        this.extensionLength = 32 // Bit
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        this.memList = emptyList<PMemInstance>()
    }

    constructor(addressLength: Int, instructionLength: Int, extensionLength: Int) {
        this.addressLength = addressLength
        this.instructionLength = instructionLength
        this.extensionLength = extensionLength
        this.globalSize = 2.0.pow(addressLength.toDouble()).toInt()
        this.memList = emptyList<PMemInstance>()
    }

    fun load(address: Int): Int? {
        for (data in memList) {
            if (data.address == address) {
                return data.value
            }
        }
        return null
    }

    fun save(address: Int, value: Int) {
        for (data in memList) {
            if (data.address == address){
                data.value = value
                return
            }
        }
        memList += PMemInstance(address, value)
    }

    fun getMaxAddress(): Int {
        return 2.0.pow(addressLength).toInt() - 1
    }

    private class PMemInstance(val address: Int, var value: Int) {

    }


}