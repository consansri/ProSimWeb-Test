package extendable

import extendable.components.*
import extendable.components.assembly.Compiler
import extendable.components.connected.*
import extendable.components.types.MutVal

import tools.DebugTools
import tools.HTMLTools
import web.buffer.Blob

/**
 *  Architecture blueprint
 *
 *  additional architectures need to build of this class
 *
 *  @param config every Architecture needs a specific config "file" which should be defined in an object which contains all configuration constants of the architecture.
 *  @param asmConfig every Architecture needs a specific Grammar and Assembler class which is then given the Architecture through the asmConfig "file"
 *
 */
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


    /**
     * continuous execution event
     *
     * should be implemented by specific archs
     */
    open fun exeContinuous() {
        getConsole().clear()
    }

    /**
     * single step execution event
     *
     * should be implemented by specific archs
     */
    open fun exeSingleStep() {
        getConsole().clear()
    }

    /**
     * multi step execution event
     *
     * should be implemented by specific archs
     */
    open fun exeMultiStep(steps: Int) {
        getConsole().clear()
        getConsole().log("--exe_multi_step $steps ...")
    }

    /**
     * skip subroutine execution event
     *
     * should be implemented by specific archs
     */
    open fun exeSkipSubroutine() {
        getConsole().clear()
    }

    /**
     * return from subroutine execution event
     *
     * should be implemented by specific archs
     */
    open fun exeReturnFromSubroutine() {
        getConsole().clear()
    }

    /**
     * until line execution event
     *
     * should be implemented by specific archs
     */
    open fun exeUntilLine(lineID: Int) {
        getConsole().clear()
    }

    /**
     * until address execution event
     *
     * should be implemented by specific archs
     */
    open fun exeUntilAddress(address: MutVal.Value.Hex){
        getConsole().clear()
    }

    /**
     * reset event
     *
     * don't need to but could be implemented by specific archs
     */
    open fun exeReset() {
        getConsole().clear()
        getConsole().log("--reset ...")
        compiler.recompile()
    }

    /**
     * clear event
     *
     * don't need to but could be implemented by specific archs
     */
    open fun exeClear() {
        getConsole().log("--clear_registers ...")
        registerContainer.clear()
    }

    /**
     * prehighlighting event
     *
     * needs to be implemented by specific architectures if prehighlighting of certain keywords is wished
     */
    open fun getPreHighlighting(line: String): String {
        val encodedLine = HTMLTools.encodeHTML(line)
        return encodedLine
    }

    /**
     * compilation event
     */
    fun check(input: String, startAtLine: Int): String {
        if (DebugTools.ARCH_showCheckCodeEvents) {
            console.log("Architecture.check(): input \n $input \n, startAtLine $startAtLine")
        }
        archState.check(compiler.setCode(input, true))

        return compiler.getHLContent()
    }

    /**
     * tool for surrounding a input with a certain highlighting html tag
     */
    fun highlight(input: String, id: Int, title: String, flag: String, vararg classNames: String): String {
        val tag = "span"
        return "<$tag class='$flag ${classNames.joinToString(" ") { it }}' id='$id'>$input</$tag>"
    }

}