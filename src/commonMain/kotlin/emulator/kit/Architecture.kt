package emulator.kit

import cengine.lang.asm.Disassembler
import cengine.lang.asm.Initializer
import cengine.util.integer.*
import cengine.util.integer.Size.*
import emulator.core.*
import emulator.kit.common.*
import emulator.kit.config.Config
import emulator.kit.memory.MainMemory
import emulator.kit.optional.Feature
import emulator.kit.optional.SetupSetting

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
 *  @property features Holds specific Assembler features.
 *  @property settings Holds specific Architecture Setup settings.
 *
 *  Possible Features
 *  @property cache Not Essential: Possibly given by Config
 *
 */
abstract class Architecture(config: Config) {
    val description: Config.Description = config.description
    val fileEnding: String = config.fileEnding
    val regContainer: RegContainer = config.regContainer
    val memory: MainMemory = config.memory
    val console: IConsole = IConsole("${config.description.name} Console")
    val features: List<Feature> = config.features
    val settings: List<SetupSetting<*>> = config.settings
    var initializer: Initializer? = null
    val disassembler: Disassembler? = config.disassembler

    init {
        // Starting with non-micro setup
        MicroSetup.clear()
        MicroSetup.append(memory)
    }

    fun getAllRegFiles(): List<RegContainer.RegisterFile> = regContainer.getRegFileList()
    fun getAllRegs(): List<RegContainer.Register> = regContainer.getAllRegs(features)
    fun getRegByName(name: String, regFile: String? = null): RegContainer.Register? = regContainer.getReg(name, features, regFile)
    fun getRegByAddr(addr: UInt, regFile: String? = null): RegContainer.Register? = regContainer.getReg(addr, features, regFile)

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
     * Execution Event: until address
     * should be implemented by specific archs
     */
    open fun exeUntilAddress(address: Hex) {
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
        initializer?.initialize(memory)
        console.exeInfo("resetting")
    }

    /**
     * Reset [MicroSetup]
     */
    fun resetMicroArch() {
        MicroSetup.clear()
        setupMicroArch()
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

}