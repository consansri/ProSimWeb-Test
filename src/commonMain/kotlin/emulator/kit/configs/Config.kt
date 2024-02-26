package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.optional.Cache

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileHandler: FileHandler,
    val regContainer: RegContainer,
    val memory: Memory,
    val transcript: Transcript,
    val cache: Cache?
) {

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory
    ) : this(description, fileHandler, regContainer, memory, Transcript(), null)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        transcript: Transcript
    ) : this(description, fileHandler, regContainer, memory, transcript, null)

    data class Description(val name: String, val fullName: String, val docs: Docs)
}