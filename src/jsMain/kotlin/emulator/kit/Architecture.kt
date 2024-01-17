package emulator.kit

import emulator.kit.assembly.Compiler
import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.optional.Cache
import emulator.kit.optional.FlagsConditions
import emulator.kit.types.Variable

import debug.DebugTools
import emulator.kit.optional.ArchSetting
import emulator.kit.optional.Feature
import react.memo
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
 *  @property description Essential: Given by Config
 *  @property fileHandler Essential: Given by Config
 *  @property regContainer Essential: Given by Config
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
    private val regContainer: RegContainer
    private val memory: Memory
    private val iConsole: IConsole
    private val archState = ArchState()
    private val compiler: Compiler
    private val transcript: Transcript
    private val flagsConditions: FlagsConditions?
    private val cache: Cache?
    private val features: List<Feature>
    private val settings: List<ArchSetting>

    init {
        this.description = config.description
        this.fileHandler = config.fileHandler
        this.regContainer = config.regContainer
        this.memory = config.memory
        this.transcript = config.transcript
        this.flagsConditions = config.flagsConditions
        this.cache = config.cache
        this.iConsole = IConsole("${config.description.name} Console")
        this.features = asmConfig.features
        this.settings = asmConfig.settings
        this.compiler = Compiler(
            this,
            asmConfig.syntax,
            asmConfig.assembly,
            asmConfig.numberSystemPrefixes,
            asmConfig.compilerDetectRegistersByNames,
            Settings.COMPILER_HLCOLL
        )
    }

    // Getter for Architecture Components
    fun getDescription(): Config.Description = description
    fun getFileHandler(): FileHandler = fileHandler
    fun getRegContainer(): RegContainer = regContainer
    fun getTranscript(): Transcript = transcript
    fun getMemory(): Memory = memory
    fun getFlagsConditions(): FlagsConditions? = flagsConditions
    fun getConsole(): IConsole = iConsole
    fun getState(): ArchState = archState
    fun getCompiler(): Compiler = compiler
    fun getFormattedFile(type: FileBuilder.ExportFormat, vararg settings: FileBuilder.Setting): Blob = FileBuilder().build(this, type, *settings)
    fun getAllFeatures(): List<Feature> = features
    fun getAllSettings(): List<ArchSetting> = settings
    fun getAllRegFiles(): List<RegContainer.RegisterFile> = regContainer.getRegFileList()
    fun getAllRegs(): List<RegContainer.Register> = regContainer.getAllRegs(features)
    fun getRegByName(name: String, regFile: String? = null): RegContainer.Register? = regContainer.getReg(name, features, regFile)
    fun getRegByAddr(addr: Variable.Value, regFile: String? = null): RegContainer.Register? = regContainer.getReg(addr, features, regFile)

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
        regContainer.clear()
        memory.clear()
        compiler.reassemble()
        getConsole().exeInfo("resetting")
    }

    /**
     * Compilation Event
     * already implemented
     */
    fun compile(input: String, build: Boolean = true): String {
        if (build) {
            regContainer.clear()
        }

        if (DebugTools.KIT_showCheckCodeEvents) {
            console.log("Architecture.check(): input \n $input \n")
        }
        archState.check(compiler.compile(input, true, build = build))

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

}