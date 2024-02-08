package emulator.archs

import emulator.archs.t6502.T6502
import emulator.archs.t6502.T6502Syntax
import emulator.kit.Architecture
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : Architecture(T6502.config, T6502.asmConfig) {


    override fun exeContinuous() {
        var counter = 0
        val measuredTime = measureTimedValue {
            while (counter < 1000) {
                val currentPC = getRegContainer().pc.get().toHex()
                val threeBytes = getMemory().loadArray(currentPC, 3)

                var paramType: T6502Syntax.AModes? = null
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                    paramType = type.opCode.entries.firstOrNull {
                        threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
                    }?.key
                    paramType != null
                } ?: break
                val actualParamType = paramType ?: break

                instrType.execute(this, actualParamType, threeBytes)
                counter++
            }
        }
        getConsole().exeInfo("$counter instructions in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeSingleStep() {
        val measuredTime = measureTimedValue {
            val currentPC = getRegContainer().pc.get().toHex()
            val threeBytes = getMemory().loadArray(currentPC, 3)

            var paramType: T6502Syntax.AModes? = null
            val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
                }?.key
                paramType != null
            } ?: return
            val actualParamType = paramType ?: return

            instrType.execute(this, actualParamType, threeBytes)
        }
        getConsole().exeInfo("1 instruction in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeMultiStep(steps: Int) {
        var counter = 0
        val measuredTime = measureTimedValue {
            while (counter < steps) {
                val currentPC = getRegContainer().pc.get().toHex()
                val threeBytes = getMemory().loadArray(currentPC, 3)

                var paramType: T6502Syntax.AModes? = null
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                    paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()

                    }?.key
                    paramType != null
                } ?: break
                val actualParamType = paramType ?: break

                instrType.execute(this, actualParamType, threeBytes)
                counter++
            }
        }
        getConsole().exeInfo("$counter instructions in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeSkipSubroutine() {
        var counter = 0
        val measureTimed = measureTimedValue {
            var currentPC = getRegContainer().pc.get().toHex()
            var threeBytes = getMemory().loadArray(currentPC, 3)

            var paramType: T6502Syntax.AModes? = null
            var instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
                }?.key
                paramType != null
            }

            if (instrType == T6502Syntax.InstrType.JSR) {
                while (counter < 1000) {
                    currentPC = getRegContainer().pc.get().toHex()
                    threeBytes = getMemory().loadArray(currentPC, 3)

                    instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                        paramType = type.opCode.entries.firstOrNull {
                            threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()
                        }?.key
                        paramType != null
                    } ?: break
                    val actualParamType = paramType ?: break

                    instrType.execute(this, actualParamType, threeBytes)
                    if (instrType == T6502Syntax.InstrType.RTS) break
                    counter++
                }
            } else {
                val actualParamType = paramType ?: return

                instrType?.execute(this, actualParamType, threeBytes)
                counter++
            }
        }

        getConsole().exeInfo("$counter instructions in ${measureTimed.duration.inWholeMicroseconds}µs")
    }

    override fun exeReturnFromSubroutine() {
        super.exeReturnFromSubroutine()

        var counter = 0
        val measureTimed = measureTimedValue {
            while (counter < 1000) {
                val currentPC = getRegContainer().pc.get().toHex()
                val threeBytes = getMemory().loadArray(currentPC, 3)

                var paramType: T6502Syntax.AModes? = null
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                    paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()

                    }?.key
                    paramType != null
                } ?: break
                val actualParamType = paramType ?: break

                instrType.execute(this, actualParamType,threeBytes)
                if (instrType == T6502Syntax.InstrType.RTS) break
                counter++
            }
        }

        getConsole().exeInfo("$counter instructions in ${measureTimed.duration.inWholeMicroseconds}µs")
    }

    override fun exeUntilLine(lineID: Int) {
        val lineAddressMap = getCompiler().getAssemblyMap().lineAddressMap.map { it.value to it.key }.filter { it.first.file == getFileHandler().getCurrent() }
        var closestID: Int? = null
        for (entry in lineAddressMap) {
            if (entry.first.lineID >= lineID) {
                if (closestID != null) {
                    if (entry.first.lineID < closestID) {
                        closestID = entry.first.lineID
                    }
                } else {
                    closestID = entry.first.lineID
                }
            }
        }
        val destAddrString = lineAddressMap.associate { it.first.lineID to it.second }[closestID] ?: return

        var counter = 0
        val measureTimed = measureTimedValue {
            while (counter < 1000) {
                val currentPC = getRegContainer().pc.get().toHex()
                if (currentPC.getRawHexStr() == destAddrString) break
                val threeBytes = getMemory().loadArray(currentPC, 3)

                var paramType: T6502Syntax.AModes? = null
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                    paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()

                    }?.key
                    paramType != null
                } ?: break
                val actualParamType = paramType ?: break

                instrType.execute(this, actualParamType, threeBytes)

                counter++
            }
        }

        getConsole().exeInfo("$counter instructions in ${measureTimed.duration.inWholeMicroseconds}µs")
    }

    override fun exeUntilAddress(address: Hex) {
        var counter = 0
        val measureTimed = measureTimedValue {
            while (counter < 1000) {
                val currentPC = getRegContainer().pc.get().toHex()

                if (currentPC.getRawHexStr() == address.getRawHexStr()) break
                val threeBytes = getMemory().loadArray(currentPC, 3)

                var paramType: T6502Syntax.AModes? = null
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { type ->
                    paramType = type.opCode.entries.firstOrNull {threeBytes.first().toHex().getRawHexStr().uppercase() == it.value.getRawHexStr().uppercase()

                    }?.key
                    paramType != null
                } ?: break
                val actualParamType = paramType ?: break

                instrType.execute(this, actualParamType, threeBytes)

                counter++
            }
        }

        getConsole().exeInfo("$counter instructions in ${measureTimed.duration.inWholeMicroseconds}µs")
    }
}