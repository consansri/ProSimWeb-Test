package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.common.memory.MainMemory
import emulator.kit.optional.ArchSetting

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileEnding: String,
    val regContainer: RegContainer,
    val memory: MainMemory,
    val settings: List<ArchSetting> = listOf()
) {
    data class Description(val name: String, val fullName: String, val docs: Docs)
}