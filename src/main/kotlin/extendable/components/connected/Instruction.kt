package extendable.components.connected

import extendable.components.connected.FlagsConditions
import extendable.components.connected.Memory
import extendable.components.connected.Register
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode
import kotlin.reflect.KClass

class Instruction(
    val name: String,
    val exFormats: List<String>,
    val opCode: OpCode,
    val pseudoCode: String,
    val description: String,
    val logic: (extensionWords: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?) -> Boolean
) {

    fun execute(extensionWords: List<ExtensionType>, mem: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean{
        return logic(extensionWords, mem, registers, flagsConditions)
    }
}