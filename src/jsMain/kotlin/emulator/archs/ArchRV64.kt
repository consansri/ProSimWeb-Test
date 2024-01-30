package emulator.archs

import emulator.archs.riscv64.RV64Syntax
import emulator.kit.Architecture
import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.kit.types.Variable
import kotlin.time.measureTime

class ArchRV64 : Architecture(RV64.config, RV64.asmConfig) {

    private val mapper = RV64BinMapper()

    override fun exeContinuous() {
        if (this.getCompiler().isBuildable()) {
            var instrCount = 0
            val measuredTime = measureTime {
                super.exeContinuous()

                var value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                while (result != null && instrCount < 1000) {
                    instrCount++
                    result.type.execute(this, result.binMap)

                    // Load next Instruction
                    value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                    result = mapper.getInstrFromBinary(value.toBin())
                }
            }
            getConsole().exeInfo("continuous \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
        }
    }

    override fun exeSingleStep() {
        if (!this.getCompiler().isBuildable()) return

        val measuredTime = measureTime {
            super.exeSingleStep() // clears console

            // load binary from memory
            val loadedValue = getMemory().load(getRegContainer().pc.get().toHex(), 4)
            // identify instr and opcode usages (build binary map)
            val instrResult = mapper.getInstrFromBinary(loadedValue.toBin())
            // execute instr
            instrResult?.type?.execute(this, instrResult.binMap)
        }

        getConsole().exeInfo("single_step \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeMultiStep(steps: Int) {
        if (this.getCompiler().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeMultiStep(steps)

                var value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                for (step in 0..<steps) {
                    if (result != null) {
                        instrCount++
                        result.type.execute(this, result.binMap)

                        // Load next Instruction
                        value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                        result = mapper.getInstrFromBinary(value.toBin())
                    } else {
                        break
                    }
                }
            }

            getConsole().exeInfo("multi_step \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeSkipSubroutine() {
        if (this.getCompiler().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeSkipSubroutine()

                var value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())
                if (result != null) {
                    if (result.type == RV64Syntax.InstrType.JAL || result.type == RV64Syntax.InstrType.JALR) {
                        val returnAddress = getRegContainer().pc.get() + Variable.Value.Hex("4", Variable.Size.Bit32())
                        while (getRegContainer().pc.get() != returnAddress) {
                            if (result != null) {
                                result.type.execute(this, result.binMap)
                                instrCount++
                            } else {
                                break
                            }
                            value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                            result = mapper.getInstrFromBinary(value.toBin())
                        }

                    } else {
                        result.type.execute(this, result.binMap)
                        instrCount++
                    }
                }
            }

            getConsole().exeInfo("exe_skip_subroutine \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeReturnFromSubroutine() {
        if (this.getCompiler().isBuildable()) {
            super.exeReturnFromSubroutine()
            var instrCount = 0
            val measuredTime = measureTime {

                var value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                var result = mapper.getInstrFromBinary(value.toBin())

                val returnTypeList = listOf(RV64Syntax.InstrType.JALR, RV64Syntax.InstrType.JAL)

                while (result != null && !returnTypeList.contains(result.type)) {
                    result.type.execute(this, result.binMap)

                    value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                    result = mapper.getInstrFromBinary(value.toBin())
                    instrCount++
                }
            }

            getConsole().exeInfo("exe_return_from_subroutine \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }

    }

    override fun exeUntilLine(lineID: Int) {
        if (this.getCompiler().isBuildable()) {
            super.exeUntilLine(lineID)
            var instrCount = 0

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
            val destAddrString = lineAddressMap.associate { it.first.lineID to it.second }[closestID] ?: ""
            if (destAddrString.isNotEmpty() && closestID != null) {
                val destAddr = Variable.Value.Hex(destAddrString)
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                        val result = mapper.getInstrFromBinary(value.toBin())
                        if (destAddr == getRegContainer().pc.get()) {
                            break
                        }
                        if (result != null) {
                            result.type.execute(this, result.binMap)
                            instrCount++
                        } else {
                            break
                        }
                    }
                }

                getConsole().exeInfo("exe_until_line\nline ${closestID + 1} or address ${destAddr.getHexStr()} \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            } else {
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                        val result = mapper.getInstrFromBinary(value.toBin())
                        if (result != null) {
                            result.type.execute(this, result.binMap)
                            instrCount++
                        } else {
                            break
                        }
                    }
                }

                getConsole().exeInfo("exe_until_line\nwasn't reaching line! \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            }
        }
    }

    override fun exeUntilAddress(address: Variable.Value.Hex) {
        super.exeUntilAddress(address)
        var instrCount = 0

        val measuredTime = measureTime {
            while (instrCount < 1000) {
                val value = getMemory().load(getRegContainer().pc.get().toHex(), 4)
                val result = mapper.getInstrFromBinary(value.toBin())
                if (address == getRegContainer().pc.get()) {
                    break
                }
                if (result != null) {
                    result.type.execute(this, result.binMap)
                    instrCount++
                } else {
                    break
                }
            }
        }
        getConsole().exeInfo("exe_until_address\nexecuting until address ${address.getHexStr()} \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
    }

}