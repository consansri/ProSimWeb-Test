package extendable

import extendable.components.DataMemory
import extendable.components.FlagsConditions
import extendable.components.ProgramMemory

open class Architecture {

    var name: String
    var programMemory: ProgramMemory
    var dataMemory: DataMemory
    open var flagsConditions: FlagsConditions? = null

    constructor(name: String, programMemory: ProgramMemory, dataMemory: DataMemory){
        this.name = name
        this.programMemory = programMemory
        this.dataMemory = dataMemory
    }

    fun getName(): String {
        return name
    }

    fun getProgramMemory(): ProgramMemory{
        return programMemory
    }

    fun getDataMemory(): DataMemory {
        return dataMemory
    }

    fun getFlagsConditions(): FlagsConditions? {
        return flagsConditions
    }







}