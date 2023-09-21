package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv32.RV32
import extendable.archs.riscv32.RV32BinMapper
import extendable.archs.riscv32.RV32Grammar
import extendable.components.types.Variable
import kotlin.time.measureTime

class ArchRV32() : Architecture(RV32.config, RV32.asmConfig) {

    override fun exeContinuous() {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0
            val measuredTime = measureTime {
                super.exeContinuous()

                val binMapper = RV32BinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                while (result != null && instrCount < 1000) {
                    instrCount++
                    result.type.execute(this, result.binMap)

                    // Load next Instruction
                    binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                    result = binMapper.getInstrFromBinary(binary.get().toBin())
                }
            }
            getConsole().log("--continuous finishing... \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
        }
    }

    override fun exeSingleStep() {
        if (this.getAssembly().isBuildable()) {
            val measuredTime = measureTime {
                super.exeSingleStep()

                val binMapper = RV32BinMapper()
                val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                val result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (result != null) {
                    result.type.execute(this, result.binMap)
                }
            }

            getConsole().log("--single_step finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
        }

    }

    override fun exeMultiStep(steps: Int) {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeMultiStep(steps)

                val binMapper = RV32BinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                for (step in 0 until steps) {
                    if (result != null) {
                        instrCount++
                        result.type.execute(this, result.binMap)

                        // Load next Instruction
                        binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                        result = binMapper.getInstrFromBinary(binary.get().toBin())
                    } else {
                        break
                    }
                }
            }

            getConsole().log("--multi_step finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeSkipSubroutine() {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0

            val measuredTime = measureTime {
                super.exeSkipSubroutine()
                val binMapper = RV32BinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (result != null) {
                    if (result.type == RV32Grammar.R_INSTR.InstrType.JAL || result.type == RV32Grammar.R_INSTR.InstrType.JALR) {
                        val returnAddress = getRegisterContainer().pc.value.get() + Variable.Value.Hex("4")
                        while (getRegisterContainer().pc.value.get() != returnAddress) {
                            if (result != null) {
                                result.type.execute(this, result.binMap)
                                instrCount++
                            } else {
                                break
                            }
                            binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                            result = binMapper.getInstrFromBinary(binary.get().toBin())
                        }

                    } else {
                        result.type.execute(this, result.binMap)
                        instrCount++
                    }
                }
            }

            getConsole().log("--exe_skip_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }
    }

    override fun exeReturnFromSubroutine() {
        if (this.getAssembly().isBuildable()) {
            super.exeReturnFromSubroutine()
            var instrCount = 0
            val measuredTime = measureTime {
                val binMapper = RV32BinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                val returnTypeList = listOf(RV32Grammar.R_INSTR.InstrType.JALR, RV32Grammar.R_INSTR.InstrType.JAL)

                while (result != null && !returnTypeList.contains(result.type)) {
                    result.type.execute(this, result.binMap)

                    binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                    result = binMapper.getInstrFromBinary(binary.get().toBin())
                    instrCount++
                }
            }

            getConsole().log("--exe_return_from_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
        }

    }

    override fun exeUntilLine(lineID: Int) {
        if (this.getAssembly().isBuildable()) {
            super.exeUntilLine(lineID)
            var instrCount = 0
            val binMapper = RV32BinMapper()
            val lineAddressMap = getAssembly().getAssemblyMap().lineAddressMap.map { it.value to it.key }.filter { it.first.file == getFileHandler().getCurrent() }
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
            val destAddrString = lineAddressMap.associate { it.first.lineID to it.second }.get(closestID) ?: ""
            if (destAddrString.isNotEmpty() && closestID != null) {
                val destAddr = Variable.Value.Hex(destAddrString)
                getConsole().info("--exe_until_line executing until line ${closestID + 1} or address ${destAddr.getHexStr()}")
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                        val result = binMapper.getInstrFromBinary(binary.get().toBin())
                        if (destAddr == getRegisterContainer().pc.value.get()) {
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

                getConsole().log("--exe_until_line finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            } else {
                getConsole().info("--exe_continuous")
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                        val result = binMapper.getInstrFromBinary(binary.get().toBin())
                        if (result != null) {
                            result.type.execute(this, result.binMap)
                            instrCount++
                        } else {
                            break
                        }
                    }
                }

                getConsole().log("--exe_continuous finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
            }
        }
    }

    override fun exeUntilAddress(address: Variable.Value.Hex) {
        super.exeUntilAddress(address)
        var instrCount = 0
        val binMapper = RV32BinMapper()
        getConsole().info("--exe_until_line executing until address ${address.getHexStr()}")
        val measuredTime = measureTime {
            while (instrCount < 1000) {
                val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                val result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (address == getRegisterContainer().pc.value.get()) {
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
        getConsole().log("--exe_until_address finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeClear() {
        val measuredTime = measureTime {
            super.exeClear()
        }
        getConsole().log("--clear finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeReset() {
        val measuredTime = measureTime {
            super.exeReset()
        }
        getConsole().log("--reset finishing... \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

}