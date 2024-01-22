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
                val opCode = getMemory().load(currentPC).toHex()
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                val amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
                counter++
            }
        }
        getConsole().exeInfo("$counter instructions in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeSingleStep() {
        val measuredTime = measureTimedValue {
            val currentPC = getRegContainer().pc.get().toHex()
            val opCode = getMemory().load(currentPC).toHex()
            val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) }
            val amode = instrType?.opCode?.map { it.value to it.key }?.toMap()?.get(opCode)

            if (instrType == null || amode == null) {
                getConsole().error("Couldn't resolve Instruction at Address ${currentPC}!")
                return
            }

            val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

            instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
        }
        getConsole().exeInfo("1 instruction in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeMultiStep(steps: Int) {
        var counter = 0
        val measuredTime = measureTimedValue {
            while (counter < steps) {
                val currentPC = getRegContainer().pc.get().toHex()
                val opCode = getMemory().load(currentPC).toHex()
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                val amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
                counter++
            }
        }
        getConsole().exeInfo("$counter instructions in ${measuredTime.duration.inWholeMicroseconds}µs")
    }

    override fun exeSkipSubroutine() {
        var counter = 0
        val measureTimed = measureTimedValue {
            var currentPC = getRegContainer().pc.get().toHex()
            var opCode = getMemory().load(currentPC).toHex()
            var instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: return
            var amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: return

            var extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

            if (instrType == T6502Syntax.InstrType.JSR) {
                while (counter < 1000) {
                    currentPC = getRegContainer().pc.get().toHex()
                    opCode = getMemory().load(currentPC).toHex()
                    instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                    amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                    extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                    instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
                    if (instrType == T6502Syntax.InstrType.RTS) break
                    counter++
                }
            } else {
                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
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
                val opCode = getMemory().load(currentPC).toHex()
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                val amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())
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

                val opCode = getMemory().load(currentPC).toHex()
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                val amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())

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

                val opCode = getMemory().load(currentPC).toHex()
                val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(opCode) } ?: break
                val amode = instrType.opCode.map { it.value to it.key }.toMap()[opCode] ?: break

                val extAddr = (currentPC + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()

                instrType.execute(this, amode, getMemory().load(extAddr).toHex(), getMemory().load(extAddr, 2).toHex())

                counter++
            }
        }

        getConsole().exeInfo("$counter instructions in ${measureTimed.duration.inWholeMicroseconds}µs")
    }
}