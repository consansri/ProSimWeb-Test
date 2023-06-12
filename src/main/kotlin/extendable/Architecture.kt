package extendable

import extendable.components.*
import extendable.components.connected.*
import tools.HTMLTools

open class Architecture(config: Config) {

    val archState = ArchState()
    val executionStartAddress = 0

    private var name: String
    private val registers: Array<Register>
    private val instructions: List<Instruction>
    private val memory: Memory
    private var transcript: Transcript
    private var flagsConditions: FlagsConditions?
    private var cache: Cache?
    private val console: Console

    init {
        this.name = config.name
        this.registers = config.register
        this.instructions = config.instructions
        this.memory = config.memory
        this.transcript = config.transcript
        this.flagsConditions = config.flagsConditions
        this.cache = config.cache
        this.console = Console("${config.name} Console")
    }

    fun getName(): String {
        return name
    }

    fun getRegister(): Array<Register> {
        return registers
    }

    fun getInstructions(): List<Instruction> {
        return instructions
    }

    fun getTranscript(): Transcript {
        return transcript
    }

    fun getDataMemory(): Memory {
        return memory
    }


    fun getFlagsConditions(): FlagsConditions? {
        return flagsConditions
    }

    fun getConsole(): Console {
        return console
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
        memory.clear()
        for (reg in registers) {
            reg.clear()
        }
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
        return "</$tag>"
    }

    protected fun highlight(input: String, flag: String): String {
        val tag = "mark"
        return "<$tag class='$flag'>$input</$tag>"
    }


    protected open fun hlAndCompile(code: String, startAtLine: Int): Pair<String, Boolean> {
        return Pair(code, true)
    }

    open fun getPreHighlighting(line: String): String {
        val encodedLine = HTMLTools.encodeBeforeHTML(line)
        return encodedLine
    }

    fun check(input: String, startAtLine: Int): String {
        var encode = HTMLTools.encodeBeforeHTML(input)
        val code = hlAndCompile(encode, startAtLine)
        archState.check(code.second)

        return code.first
    }

}