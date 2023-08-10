package extendable.components

import extendable.components.connected.*

data class Config(
    val name: String,
    val docs: Docs,
    val fileHandler: FileHandler,
    val registerContainer: RegisterContainer,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {

    constructor(
        name: String,
        docs: Docs,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory
    ) : this(name, docs, fileHandler, registerContainer, memory, Transcript(), null, null)

    constructor(
        name: String,
        docs: Docs,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
    ) : this(name, docs, fileHandler, registerContainer, memory, transcript, null, null)

    constructor(
        name: String,
        docs: Docs,
        fileHandler: FileHandler,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, docs, fileHandler, registerContainer, memory, transcript, flagsConditions, null)
}