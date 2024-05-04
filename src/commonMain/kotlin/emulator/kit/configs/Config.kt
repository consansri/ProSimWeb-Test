package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.optional.Cache

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileEnding: String,
    val regContainer: RegContainer,
    val memory: Memory,
    val cache: Cache?
) {

    constructor(
        description: Description,
        fileEnding: String,
        regContainer: RegContainer,
        memory: Memory
    ) : this(description, fileEnding, regContainer, memory, null)

    data class Description(val name: String, val fullName: String, val docs: Docs)
}