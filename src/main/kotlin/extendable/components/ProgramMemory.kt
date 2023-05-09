package extendable.components

import kotlin.math.pow

class ProgramMemory {

    val instructionLength: Int // in Bit
    val extensionLength: Int // in Bit
    val initNumber: Byte = 0

    lateinit var memArray: Array<Array<Byte>>

    constructor() {
        this.instructionLength = 32 // Bit
        this.extensionLength = 32 // Bit

        setup()
    }

    constructor(instructionLength: Int, extensionLength: Int) {
        this.instructionLength = instructionLength
        this.extensionLength = extensionLength

        setup()
    }

    private fun setup() {
        
    }



}