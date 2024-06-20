package emulator.kit.config

import emulator.kit.common.*
import emulator.kit.memory.MainMemory
import emulator.kit.optional.SetupSetting

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileEnding: String,
    val regContainer: RegContainer,
    val memory: MainMemory,
    val settings: List<SetupSetting<*>> = listOf()
) {
    data class Description(val name: String, val fullName: String, val docs: Docs)
}