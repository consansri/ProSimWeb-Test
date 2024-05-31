package emulator.kit.common

import emulator.kit.common.memory.Memory

object MicroSetup {
    val memory = mutableListOf<Memory>()

    fun append(mem: Memory){
        memory.add(mem)
    }

    fun clear() {
        memory.clear()
    }
}