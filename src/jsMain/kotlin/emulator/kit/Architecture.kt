package emulator.kit

import emulator.archs.riscv32.RV32Flags
import emulator.kit.assembly.Compiler
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.optional.Cache
import emulator.kit.optional.FlagsConditions
import emulator.kit.types.Variable

import tools.DebugTools
import tools.HTMLTools
import web.buffer.Blob

/**
 *  Architecture Blueprint
 *
 *  Additional architectures need to build of this class.
 *  This Architecture gets a lot of it's logic through the constructor. To get the Processor in another State this architecture contains the main execution and syntax events.
 *  While the compilation process is fully integrated if the Syntax Logic is given through an abstracted Grammar and Assembly Class. The Execution Process needs to be implemented in the events.
 *  To make Debugging simpler i would recommend to implement a binary mapper which maps a certain instruction with it's parameters to a binary representation and the other way around.
 *  I would recommend you to look at integration of RV32I Assembler, Grammar and Binary Mapper as an example.
 *
 *  @param config Specific config "file" which should be defined in an object which contains all configuration constants of the architecture.
 *  @param asmConfig Specific Grammar and Assembler class which is then given the Architecture through the asmConfig "file"
 *
 *  Essential Features
 *  @property name Essential: Given by Config
 *  @property docs Essential: Given by Config
 *  @property fileHandler Essential: Given by Config
 *  @property registerContainer Essential: Given by Config
 *  @property memory Essential: Given by Config
 *  @property transcript Essential: Given by Config
 *
 *  @property iConsole Instantiated with Config name
 *  @property archState Instantiated
 *  @property compiler Instantiated with AsmConfig grammar and assembly and ArchConst COMPILER_REGEX and StandardHL
 *
 *  Possible Features
 *  @property flagsConditions Not Essential: Possibly given by Config
 *  @property cache Not Essential: Possibly given by Config
 *
 */
abstract class Architecture(config: Config, asmConfig: AsmConfig) {

    private val description: Config.Description
    private val fileHandler: FileHandler
    private val registerContainer: RegisterContainer
    private val memory: Memory
    private val iConsole: IConsole
    private val archState = ArchState()
    private val compiler: Compiler
    private val transcript: Transcript
    private val flagsConditions: FlagsConditions?
    private val cache: Cache?

    init {
        this.description = config.description
        this.fileHandler = config.fileHandler
        this.registerContainer = config.registerContainer
        this.memory = config.memory
        this.transcript = config.transcript
        this.flagsConditions = config.flagsConditions
        this.cache = config.cache
        this.iConsole = IConsole("${config.description.name} Console")

        this.compiler = Compiler(
            this,
            asmConfig.syntax,
            asmConfig.assembly,
            Settings.COMPILER_REGEX,
            Settings.COMPILER_HLCOLL
        )
    }

    fun getDescription(): Config.Description = description
    fun getFileHandler(): FileHandler = fileHandler
    fun getRegisterContainer(): RegisterContainer = registerContainer
    fun getTranscript(): Transcript = transcript
    fun getMemory(): Memory = memory
    fun getFlagsConditions(): FlagsConditions? = flagsConditions
    fun getConsole(): IConsole = iConsole
    fun getState(): ArchState = archState
    fun getAssembly(): Compiler = compiler
    fun getFormattedFile(type: FileBuilder.ExportFormat, vararg settings: FileBuilder.Setting): Blob = FileBuilder().build(this, type, *settings)

    /**
     * Execution Event: continuous
     * should be implemented by specific archs
     */
    open fun exeContinuous() {
        getConsole().clear()
    }

    /**
     * Execution Event: single step
     * should be implemented by specific archs
     */
    open fun exeSingleStep() {
        getConsole().clear()
    }

    /**
     * Execution Event: multi step
     * should be implemented by specific archs
     */
    open fun exeMultiStep(steps: Int) {
        getConsole().clear()
        getConsole().log("--exe_multi_step $steps ...")
    }

    /**
     * Execution Event: skip subroutine
     * should be implemented by specific archs
     */
    open fun exeSkipSubroutine() {
        getConsole().clear()
    }

    /**
     * Execution Event: return from subroutine
     * should be implemented by specific archs
     */
    open fun exeReturnFromSubroutine() {
        getConsole().clear()
    }

    /**
     * Execution Event: until line
     * should be implemented by specific archs
     */
    open fun exeUntilLine(lineID: Int) {
        getConsole().clear()
    }

    /**
     * Execution Event: until address
     * should be implemented by specific archs
     */
    open fun exeUntilAddress(address: Variable.Value.Hex) {
        getConsole().clear()
    }

    /**
     * Reset Event
     * don't need to but could be implemented by specific archs
     */
    open fun exeReset() {
        getConsole().clear()
        getConsole().log("--reset ...")
        compiler.reassemble()
    }

    /**
     * Clear Event
     * don't need to but could be implemented by specific archs
     */
    open fun exeClear() {
        getConsole().log("--clear_registers ...")
        registerContainer.clear()
    }

    /**
     * PreHighlight Event
     * needs to be implemented by specific architectures if prehighlighting of certain keywords is wished
     */
    open fun getPreHighlighting(line: String): String {
        val encodedLine = HTMLTools.encodeHTML(line)
        return encodedLine
    }

    /**
     * Compilation Event
     * already implemented
     */
    fun check(input: String, startAtLine: Int): String {
        if (DebugTools.ARCH_showCheckCodeEvents) {
            console.log("Architecture.check(): input \n $input \n, startAtLine $startAtLine")
        }
        archState.check(compiler.setCode(input, true))

        return compiler.getHLContent()
    }

    /**
     * Tool
     * for surrounding a input with a certain highlighting html tag
     */
    fun highlight(input: String, id: Int? = null, title: String, flag: String, vararg classNames: String): String {
        val tag = "span"
        return "<$tag class='$flag ${classNames.joinToString(" ") { it }}' ${id?.let { "id='$id'" }}>$input</$tag>"
    }

    fun hlText(input: String, hlPatterns: List<Regex>, title: String, flag: String): String {
        var result = input
        for (pattern in hlPatterns) {
            result = result.replace(pattern) {
                val match = if(it.groupValues.size == 3){
                    it.groupValues[1]
                }else{
                    it.groupValues.first()
                }
                highlight(match, title = title, flag = flag)

            }
        }
        return result
    }

    data class PreHLRepl(val element: String, val replacement: String)
}