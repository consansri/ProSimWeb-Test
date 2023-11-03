package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.optional.Cache
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
    val featureStates: Map<String, Boolean> = mapOf()
) {

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        featureStates: Map<String, Boolean> = mapOf()
    ) : this(description, fileHandler, regContainer, memory, Transcript(), null, null, featureStates)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        transcript: Transcript,
        featureStates: Map<String, Boolean> = mapOf()
    ) : this(description, fileHandler, regContainer, memory, transcript, null, null, featureStates)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        regContainer: RegContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?,
        featureStates: Map<String, Boolean> = mapOf()
    ) : this(description, fileHandler, regContainer, memory, transcript, flagsConditions, null, featureStates)

    data class Description(val name: String, val fullName: String, val docs: Docs)
}