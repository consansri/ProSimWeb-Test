package emulator.kit.config

import cengine.lang.asm.Disassembler
import emulator.kit.common.Docs
import emulator.kit.common.RegContainer
import emulator.kit.memory.MainMemory
import emulator.kit.optional.Feature
import emulator.kit.optional.SetupSetting

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileEnding: String,
    val regContainer: RegContainer,
    val memory: MainMemory,
    val disassembler: Disassembler?,
    val settings: List<SetupSetting<*>> = listOf(),
    val features: List<Feature> = listOf()
) {
    data class Description(val name: String, val fullName: String, val docs: Docs)
}