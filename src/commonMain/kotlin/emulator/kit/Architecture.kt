package emulator.kit

import emulator.kit.common.*
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable

import debug.DebugTools
import emulator.kit.assembler.*
import emulator.kit.assembler.DefinedAssembly
import emulator.kit.common.memory.MainMemory
import emulator.kit.optional.ArchSetting
import emulator.kit.optional.Feature

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
 *  @property regContainer Essential: Given by Config
 *  @property memory Essential: Given by Config
 *
 *  @property console Instantiated with Config name
 *  @property assembler Instantiated with AsmConfig grammar and assembly and ArchConst COMPILER_REGEX and StandardHL
 *
 *  Possible Features
 *  @property cache Not Essential: Possibly given by Config
 *
 */
abstract class Architecture(config: Config, asmConfig: AsmConfig) {
    val description: Config.Description
    val fileEnding: String
    val regContainer: RegContainer
    val memory: MainMemory
    val console: IConsole
    val assembler: Assembler
    val features: List<Feature>
    val settings: List<ArchSetting>
    private var lastFile: AsmFile? = null
    private val definedAssembly: DefinedAssembly

    init {
        // Build Arch from Config
        this.description = config.description
        this.fileEnding = config.fileEnding
        this.regContainer = config.regContainer
        this.memory = config.memory
        this.console = IConsole("${config.description.name} Console")
        this.settings = config.settings
        this.features = asmConfig.features
        this.definedAssembly = asmConfig.definedAssembly
        this.assembler = Assembler(
            this,
            asmConfig.definedAssembly
        )

        // Starting with non micro setup
        MicroSetup.clear()
        MicroSetup.append(memory)
    }

    fun getFormattedFile(type: FileBuilder.ExportFormat, currentFile: AsmFile, vararg settings: FileBuilder.Setting): List<String> = FileBuilder.buildFileContentLines(this, type, currentFile, *settings)
    fun getAllRegFiles(): List<RegContainer.RegisterFile> = regContainer.getRegFileList()
    fun getAllRegs(): List<RegContainer.Register> = regContainer.getAllRegs(features)
    fun getAllInstrTypes(): List<InstrTypeInterface> = assembler.parser.getInstrs(features)
    fun getAllDirTypes(): List<DirTypeInterface> = assembler.parser.getDirs(features)
    fun getRegByName(name: String, regFile: String? = null): RegContainer.Register? = regContainer.getReg(name, features, regFile)
    fun getRegByAddr(addr: Variable.Value, regFile: String? = null): RegContainer.Register? = regContainer.getReg(addr, features, regFile)

    /**
     * Execution Event: continuous
     * should be implemented by specific archs
     */
    open fun exeContinuous() {
        console.clear()
    }

    /**
     * Execution Event: single step
     * should be implemented by specific archs
     */
    open fun exeSingleStep() {
        console.clear()
    }

    /**
     * Execution Event: multi step
     * should be implemented by specific archs
     */
    open fun exeMultiStep(steps: Long) {
        console.clear()
    }

    /**
     * Execution Event: skip subroutine
     * should be implemented by specific archs
     */
    open fun exeSkipSubroutine() {
        console.clear()
    }

    /**
     * Execution Event: return from subroutine
     * should be implemented by specific archs
     */
    open fun exeReturnFromSubroutine() {
        console.clear()
    }

    /**
     * Execution Event: until line
     * should be implemented by specific archs
     */
    open fun exeUntilLine(lineID: Int, wsRelativeFileName: String) {
        console.clear()
    }

    /**
     * Execution Event: until address
     * should be implemented by specific archs
     */
    open fun exeUntilAddress(address: Variable.Value.Hex) {
        console.clear()
    }

    /**
     * Reset Event
     * don't need to but could be implemented by specific archs
     */
    open fun exeReset() {
        regContainer.clear()
        MicroSetup.getMemoryInstances().forEach {
            it.clear()
        }

        console.exeInfo("resetting")
    }

    /**
     * Reset [MicroSetup]
     */
    fun resetMicroArch() {
        MicroSetup.clear()
        setupMicroArch()
        console.exeInfo("Loaded (${description.fullName}) Micro Architecture Setup: ${MicroSetup}")
        nativeInfo("Loaded (${description.fullName}) Micro Architecture Setup: ${MicroSetup}")
        //nativeLog(MicroSetup.toString())
    }

    /**
     * Setup [MicroSetup]
     * For visibility of certain micro architectural components.
     *
     * Append Components in use to [MicroSetup] to make them visible.
     */
    open fun setupMicroArch() {
        MicroSetup.append(memory)
    }

    /**
     * Compilation Event
     * already implemented
     */
    fun compile(mainFile: AsmFile, others: List<AsmFile>, build: Boolean = true): Process.Result {
        if (build) {
            exeReset()
        }

        if (DebugTools.KIT_showCheckCodeEvents) {
            println("Architecture.check(): input \n $mainFile \n")
        }

        val mode = if (build) Process.Mode.FULLBUILD else Process.Mode.STOP_AFTER_ANALYSIS

        val compilationResult = assembler.compile(mainFile, others, mode)
        return compilationResult
    }

}