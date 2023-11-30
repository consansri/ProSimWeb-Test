package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.optional.Cache
import emulator.kit.optional.Feature
import emulator.kit.optional.FlagsConditions

/**
 * [Config] is the configuration Class which holds all common and optional implementations for defining each specific architecture.
 */
data class Config(
    val description: Description,
    val fileHandler: FileHandler,
    val regContainer: RegContainer,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?,
    val features: List<Feature> = listOf()
) {

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        features: List<Feature> = listOf()
    ) : this(description, fileHandler, regContainer, memory, Transcript(), null, null, features)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        transcript: Transcript,
        features: List<Feature> = listOf()
    ) : this(description, fileHandler, regContainer, memory, transcript, null, null, features)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?,
        features: List<Feature> = listOf()
    ) : this(description, fileHandler, regContainer, memory, transcript, flagsConditions, null, features)

    data class Description(val name: String, val fullName: String, val docs: Docs)
}