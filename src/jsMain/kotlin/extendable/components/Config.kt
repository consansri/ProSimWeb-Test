package extendable.components

import extendable.components.connected.*

data class Config(
    val name: String,
    val registerContainer: RegisterContainer,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {

    constructor(
        name: String,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript
    ) : this(name, registerContainer,  memory, transcript, null, null)

    constructor(
        name: String,
        registerContainer: RegisterContainer,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, registerContainer,  memory, transcript, flagsConditions, null)
}