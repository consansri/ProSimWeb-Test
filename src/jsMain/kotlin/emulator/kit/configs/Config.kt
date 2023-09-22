package emulator.kit.configs

import emulator.kit.common.*
import emulator.kit.optional.Cache
import emulator.kit.optional.FlagsConditions

data class Config(
    val description: Description,
    val fileHandler: FileHandler,
    val registerContainer: RegisterContainer,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {

    constructor(
        description: Description,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory
    ) : this(description, fileHandler, registerContainer, memory, Transcript(), null, null)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
    ) : this(description, fileHandler, registerContainer, memory, transcript, null, null)

    constructor(
        description: Description,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(description, fileHandler, registerContainer, memory, transcript, flagsConditions, null)

    data class Description(val name: String, val fullName: String, val docs: Docs)
}