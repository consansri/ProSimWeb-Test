package extendable.components.connected

import extendable.components.connected.FlagsConditions
import extendable.components.connected.Memory
import extendable.components.connected.Register
import extendable.components.types.OpCode

class Instruction(
    val name: String,
    val extensionCount: Int,
    val format: String,
    val opCode: OpCode,
    val pseudoCode: String,
    val description: String,
    val logic: (extensionWords: List<String>, mem: Memory, registers: Array<Register>) -> Boolean
) {

    var registers: Array<Register>? = null
    var mem: Memory? = null
    var flagsConditions: FlagsConditions? = null

    fun execute(extensionWords: List<String>, mem: Memory, registers: Array<Register>): Boolean{
        this.registers = registers
        this.mem = mem

        val success = logic(extensionWords, mem, registers)

        this.registers = null
        this.mem = null
        return success
    }
    fun execute(extensionWords: List<String>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions): Boolean{
        this.registers = registers
        this.mem = mem
        this.flagsConditions = flagsConditions

        val success = logic(extensionWords, mem, registers)

        this.registers = null
        this.mem = null
        this.flagsConditions = null
        return success
    }
}