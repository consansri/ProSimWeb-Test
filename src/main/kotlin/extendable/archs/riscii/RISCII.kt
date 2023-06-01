package extendable.archs.riscii

import extendable.components.*
import extendable.components.connected.Instruction
import extendable.components.connected.Memory
import extendable.components.connected.Register
import extendable.components.connected.Transcript
import extendable.components.types.OpCode

object RISCII {

    val config = Config(
        "IKR RISC-II",
        arrayOf(
            Register(0, "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name",3, "[name] , , ", OpCode("0101010"), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<String>, memory: Memory, registers: Array<Register>): Boolean {
        return false
    }

}