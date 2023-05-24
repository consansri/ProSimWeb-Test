package extendable.components

import kotlin.math.pow

class Transcript {

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

    private class PMemInstance(val address: Int, var value: Int, var flag: String) {

    }


}