package extendable

import extendable.archs.riscv.RISCVFlags
import extendable.components.*
import tools.HTMLTools

open class Architecture {

    private var name: String
    private val register: Array<Register>
    private val instructions: List<Instruction>
    private val dataMemory: DataMemory
    private var transcript: Transcript
    private var flagsConditions: FlagsConditions?
    private var cache: Cache?

    constructor(
        config: Config
    ) {
        this.name = config.name
        this.register = config.register
        this.instructions = config.instructions
        this.dataMemory = config.dataMemory
        this.transcript = config.transcript
        this.flagsConditions = config.flagsConditions
        this.cache = config.cache
    }

    fun getName(): String {
        return name
    }

    fun getRegister(): Array<Register> {
        return register
    }

    fun getInstructions(): List<Instruction> {
        return instructions
    }

    fun getTranscript(): Transcript {
        return transcript
    }

    fun getDataMemory(): DataMemory {
        return dataMemory
    }


    fun getFlagsConditions(): FlagsConditions? {
        return flagsConditions
    }


    /*Execution Events*/
    open fun exeContinuous() {

    }

    open fun exeSingleStep() {

    }

    open fun exeMultiStep(steps: Int) {

    }

    open fun exeSkipSubroutines() {

    }

    open fun exeSubroutine() {

    }

    open fun exeClear() {
        dataMemory.clear()
    }

    private fun highlightNumbers(input: String): String {
        val tag = "mark"
        val decimalRegex = Regex("""#\d+""")
        val hexRegex = Regex("""&[0-9a-fA-F]+""")
        var output = input.replace(decimalRegex) { matchResult ->
            "<$tag class='decimal' >${matchResult.value}</$tag>"
        }
        output = output.replace(hexRegex) { matchResult ->
            "<$tag class='hex' >${matchResult.value}</$tag>"
        }
        return output
    }

    protected fun highlightKeyWords(input: String, keywords: Array<String>, flag: String): String {
        val tag = "mark"
        val regex = Regex("\\b(${keywords.joinToString("|")})\\b", RegexOption.IGNORE_CASE)

        return input.replace(regex) { matchResult ->
            "<$tag class='$flag' >${matchResult.value}</$tag>"
        }
    }

    protected fun highlightBeginTag(flag: String): String {
        val tag = "mark"
        return "<$tag class='$flag'>"
    }

    protected fun highlightEndTag(): String {
        val tag = "mark"
        return  "</$tag>"
    }

    protected fun highlight(input: String, flag: String): String {
        val tag = "mark"
        return "<$tag class='$flag'>$input</$tag>"
    }

    open fun highlightArchSyntax(code: String): String {


        return code
    }

    fun getHighlightedInput(input: String): String {
        var output = HTMLTools.encodeBeforeHTML(input)
        output = highlightArchSyntax(output)

        return output
    }
}