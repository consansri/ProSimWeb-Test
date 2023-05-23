package extendable

import extendable.components.DataMemory
import extendable.components.FlagsConditions
import extendable.components.ProgramMemory
import extendable.components.Register
import tools.HTMLTools

open class Architecture {

    private var name: String
    private var programMemory: ProgramMemory
    private var dataMemory: DataMemory
    private val register: Array<Register>
    open var flagsConditions: FlagsConditions? = null

    constructor(name: String, programMemory: ProgramMemory, dataMemory: DataMemory, register: Array<Register>) {
        this.name = name
        this.programMemory = programMemory
        this.dataMemory = dataMemory
        this.register = register
    }

    fun getName(): String {
        return name
    }

    fun getProgramMemory(): ProgramMemory {
        return programMemory
    }

    fun getDataMemory(): DataMemory {
        return dataMemory
    }

    fun getRegister(): Array<Register> {
        return register
    }

    fun getFlagsConditions(): FlagsConditions? {
        return flagsConditions
    }

    private fun highlightKeyWords(input: String, keywords: Array<String>, className: String): String {
        val tag = "mark"
        val regex = Regex("\\b(${keywords.joinToString("|")})\\b", RegexOption.IGNORE_CASE)

        return input.replace(regex) { matchResult ->
            "<$tag class='$className' >${matchResult.value}</$tag>"
        }
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

    fun getHighlightedInput(input: String): String {
        val inputEncoded = HTMLTools.encodeBeforeHTML(input)
        val hlWords = highlightKeyWords(inputEncoded, arrayOf<String>("load", "add"), "blue")
        val hlNumbers = highlightNumbers(hlWords)
        val hlNumbersEncoded = HTMLTools.encodeAfterHTML(hlNumbers)

        return hlNumbersEncoded
    }
}