package extendable

import extendable.components.*
import extendable.components.assembly.Compiler
import extendable.components.connected.*
import extendable.components.types.MutVal

import tools.DebugTools
import tools.HTMLTools
import web.buffer.Blob

abstract class Architecture(config: Config, asmConfig: AsmConfig) {

    private val name: String
    private val docs: Docs
    private val fileHandler: FileHandler
    private val registerContainer: RegisterContainer
    private val memory: Memory
    private val IConsole: IConsole
    private val archState = ArchState()
    private val compiler: Compiler
    private val transcript: Transcript
    private val flagsConditions: FlagsConditions?
    private val cache: Cache?


    init {
        this.name = config.name
        this.docs = config.docs
        this.fileHandler = config.fileHandler
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
            ArchConst.COMPILER_REGEX,
            ArchConst.StandardHL.COMPILER_COLL
        )
    }

    fun getName(): String {
        return name
    }

    fun getDocs(): Docs {
        return docs
    }

    fun getFileHandler(): FileHandler {
        return fileHandler
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

    fun getFormattedFile(type: FileBuilder.ExportFormat, vararg settings: FileBuilder.Setting): Blob = FileBuilder().build(this, type, *settings)


    /*Execution Events*/
    open fun exeContinuous() {
        getConsole().clear()
    }

    open fun exeSingleStep() {
        getConsole().clear()
    }

    open fun exeMultiStep(steps: Int) {
        getConsole().clear()
        getConsole().log("--exe_multi_step $steps ...")
    }

    open fun exeSkipSubroutine() {
        getConsole().clear()
    }

    open fun exeReturnFromSubroutine() {
        getConsole().clear()
    }

    open fun exeUntilLine(lineID: Int) {
        getConsole().clear()
    }

    open fun exeUntilAddress(address: MutVal.Value.Hex){
        getConsole().clear()
    }

    open fun exeReset() {
        getConsole().clear()
        getConsole().log("--reset ...")
        registerContainer.pc.reset()
        getAssembly().recompile()
    }

    open fun exeClear() {
        getConsole().log("--clear_registers ...")
        registerContainer.clear()
    }

    /* Compilation Event */
    fun highlight(input: String, id: Int, title: String, flag: String, vararg classNames: String): String {
        val tag = "span"
        return "<$tag class='$flag ${classNames.joinToString(" ") { it }}' id='$id'>$input</$tag>"
    }

    open fun getPreHighlighting(line: String): String {
        val encodedLine = HTMLTools.encodeHTML(line)
        return encodedLine
    }

    fun check(input: String, startAtLine: Int): String {
        if (DebugTools.ARCH_showCheckCodeEvents) {
            console.log("Architecture.check(): input \n $input \n, startAtLine $startAtLine")
        }
        archState.check(compiler.setCode(input, true)) // TODO(if certain CodeSize is reached disable highlighting!)

        return compiler.getHLContent()
    }

    enum class EXETYPE {
        CONTINUOUS,
        SSTEP,
        MSTEP,
        EXESUB,
        RETSUB
    }

}