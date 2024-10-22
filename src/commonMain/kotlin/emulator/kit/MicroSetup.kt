package emulator.kit

import emulator.kit.memory.Memory

/**
 * [MicroSetup] Always holds the Architecture Components to expose them to the UI.
 *
 * This Object will be setup by each arch through [Architecture.setupMicroArch].
 */
object MicroSetup {
    private val memory = mutableListOf<Memory>()

    fun getMemoryInstances(): List<Memory> = memory

    fun append(mem: Memory) {
        memory.add(mem)
    }

    fun clear() {
        memory.clear()
    }

    override fun toString(): String {
        return """
            Setup:
                mems: ${memory.joinToString { it.name }}
                            
        """.trimIndent()
    }
}