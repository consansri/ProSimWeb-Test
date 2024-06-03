package emulator.kit

import emulator.kit.common.memory.Memory

object MicroSetup {
    private val memory = mutableListOf<Memory>()


    fun getMemoryInstances(): List<Memory> = memory

    fun append(mem: Memory){
        memory.add(mem)
    }

    fun clear() {
        memory.clear()
    }

    override fun toString(): String {
        return """
            MicroSetup:
                mems: ${memory.joinToString { it.name }}
                            
        """.trimIndent()
    }
}