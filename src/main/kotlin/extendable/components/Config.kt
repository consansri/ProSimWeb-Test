package extendable.components

data class Config(
    val name: String,
    val register: Array<Register>,
    val instructions: List<Instruction>,
    val dataMemory: DataMemory,
    val transcript: Transcript,
    val flagsConditions: FlagsConditions?,
    val cache: Cache?
) {
    constructor(
        name: String,
        register: Array<Register>,
        instructions: List<Instruction>,
        dataMemory: DataMemory,
        transcript: Transcript
    ) : this(name, register, instructions, dataMemory, transcript, null, null)

    constructor(
        name: String,
        register: Array<Register>,
        instructions: List<Instruction>,
        dataMemory: DataMemory,
        transcript: Transcript,
        flagsConditions: FlagsConditions?
    ) : this(name, register, instructions, dataMemory, transcript, flagsConditions, null)
}