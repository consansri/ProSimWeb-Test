package extendable.components

import extendable.components.connected.*

data class Config(
    val name: String,
    val fileHandler: FileHandler,
    val registerContainer: RegisterContainer,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {

    constructor(
        name: String,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory
    ) : this(name, fileHandler, registerContainer, memory, Transcript(), null, null)

    constructor(
        name: String,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript
    ) : this(name, fileHandler, registerContainer, memory, transcript, null, null)

    constructor(
        name: String,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, fileHandler, registerContainer, memory, transcript, flagsConditions, null)
}