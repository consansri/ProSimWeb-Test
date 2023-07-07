package extendable

import extendable.components.*
import extendable.components.assembly.Compiler
import extendable.components.connected.*
import tools.HTMLTools

abstract class Architecture(config: Config, asmConfig: AsmConfig) {

    private var name: String
    private val registerContainer: RegisterContainer
    private val memory: Memory
    private var transcript: Transcript
    private var flagsConditions: FlagsConditions?
    private var cache: Cache?
    private val IConsole: IConsole
    private val archState = ArchState()
    private val compiler: Compiler
    private val executionStartAddress = 0


    init {
        this.name = config.name
        this.registerContainer = config.registerContainer
        this.memory = config.memory
        this.transcript = config.transcript
        this.flagsConditions = config.flagsConditions
        this.cache = config.cache
        this.IConsole = IConsole("${config.name} Console")

        this.compiler = Compiler(
            this,
            asmConfig.grammar,
            asmConfig.assembly,
            Compiler.RegexCollection(
                Regex("""^\s+"""),
                Regex("""^[^0-9A-Za-z]"""),
                Regex("""^${ArchConst.PRESTRING_BINARY}[01]+"""),
                Regex("""^${ArchConst.PRESTRING_HEX}[0-9a-f]+""", RegexOption.IGNORE_CASE),
                Regex("""^${ArchConst.PRESTRING_DECIMAL}-[0-9]+"""),
                Regex("""^${ArchConst.PRESTRING_DECIMAL}[0-9]+"""),
                Regex("""^'.+'"""),
                Regex("""^[a-z][a-z0-9]*""", RegexOption.IGNORE_CASE),
                Regex("""^[a-z]+""", RegexOption.IGNORE_CASE)
            ),
            Compiler.HLFlagCollection(
                alphaNum = ArchConst.StandardHL.alphaNum,
                word = ArchConst.StandardHL.word,
                const_hex = ArchConst.StandardHL.hex,
                const_bin = ArchConst.StandardHL.bin,
                const_dec = ArchConst.StandardHL.dec,
                const_udec = ArchConst.StandardHL.udec,
                const_ascii = ArchConst.StandardHL.ascii,
                register = ArchConst.StandardHL.register,
                symbol = ArchConst.StandardHL.symbol,
                instruction = ArchConst.StandardHL.instruction,
                comment = ArchConst.StandardHL.comment,
                //whitespace = ArchConst.StandardHL.whiteSpace
            )
        )
    }

    fun getName(): String {
        return name
    }

    fun getRegisterContainer(): RegisterContainer {
        return registerContainer
    }

    fun getTranscript(): Transcript {
        return transcript
    }

    fun getMemory(): Memory {
        return memory
    }

    fun getFlagsConditions(): FlagsConditions? {
        return flagsConditions
    }

    fun getConsole(): IConsole {
        return IConsole
    }

    fun getState(): ArchState {
        return archState
    }

    fun getAssembly(): Compiler {
        return compiler
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
        registerContainer.clear()
    }

    fun highlight(input: String, id: Int, title: String, flag: String, vararg classNames: String): String {
        val tag = "span"
        return "<$tag class='$flag ${classNames.joinToString(" ") { it }}' id='$id'>$input</$tag>"
    }


    abstract fun hlAndCompile(code: String, startAtLine: Int): Compiler.CompilationResult

    open fun getPreHighlighting(line: String): String {
        val encodedLine = HTMLTools.encodeBeforeHTML(line)
        return encodedLine
    }

    fun check(input: String, startAtLine: Int): String {
        compiler.setCode(input, true) // TODO(if certain CodeSize is reached disable highlighting!)
        var encode = HTMLTools.encodeBeforeHTML(input)
        val code = hlAndCompile(encode, startAtLine)
        archState.check(code.buildable)

        return code.highlightedContent
    }

}