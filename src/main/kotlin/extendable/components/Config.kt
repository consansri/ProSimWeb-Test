package extendable.components

import extendable.components.connected.*

data class Config(
    val name: String,
    val registerContainer: RegisterContainer,
    val instructions: List<Instruction>,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {

    constructor(
        name: String,
        registerContainer: RegisterContainer,
        instructions: List<Instruction>,
        memory: Memory,
        transcript: Transcript
    ) : this(name, registerContainer, instructions, memory, transcript, null, null)

    constructor(
        name: String,
        registerContainer: RegisterContainer,
        instructions: List<Instruction>,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, registerContainer, instructions, memory, transcript, flagsConditions, null)
}