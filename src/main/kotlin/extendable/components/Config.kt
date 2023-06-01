package extendable.components

import extendable.components.connected.*

data class Config(
    val name: String,
    val register: Array<Register>,
    val instructions: List<Instruction>,
    val memory: Memory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {
    constructor(
        name: String,
        register: Array<Register>,
        instructions: List<Instruction>,
        memory: Memory,
        transcript: Transcript
    ) : this(name, register, instructions, memory, transcript, null, null)

    constructor(
        name: String,
        register: Array<Register>,
        instructions: List<Instruction>,
        memory: Memory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, register, instructions, memory, transcript, flagsConditions, null)
}