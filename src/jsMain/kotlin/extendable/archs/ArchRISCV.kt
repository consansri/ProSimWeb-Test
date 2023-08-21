package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVBinMapper
import extendable.archs.riscv.RISCVGrammar
import extendable.components.types.MutVal
import kotlin.time.measureTime

class ArchRISCV() : Architecture(RISCV.config, RISCV.asmConfig) {

    override fun exeContinuous() {
        if (this.getAssembly().isBuildable()) {
            var instrCount = 0
            val measuredTime = measureTime {
                super.exeContinuous()

                val binMapper = RISCVBinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                while (result != null && instrCount < 1000) {
                    instrCount++
                    result.type.execute(this, result.binaryMap)

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

                val binMapper = RISCVBinMapper()
                val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                val result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (result != null) {
                    result.type.execute(this, result.binaryMap)
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

                val binMapper = RISCVBinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                for (step in 0 until steps) {
                    if (result != null) {
                        instrCount++
                        result.type.execute(this, result.binaryMap)

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
                val binMapper = RISCVBinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (result != null) {
                    if (result.type == RISCVGrammar.R_INSTR.InstrType.JAL || result.type == RISCVGrammar.R_INSTR.InstrType.JALR) {
                        val returnAddress = getRegisterContainer().pc.value.get() + MutVal.Value.Hex("4")
                        while (getRegisterContainer().pc.value.get() != returnAddress) {
                            if (result != null) {
                                result.type.execute(this, result.binaryMap)
                                instrCount++
                            } else {
                                break
                            }
                            binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                            result = binMapper.getInstrFromBinary(binary.get().toBin())
                        }

                    } else {
                        result.type.execute(this, result.binaryMap)
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
                val binMapper = RISCVBinMapper()
                var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                var result = binMapper.getInstrFromBinary(binary.get().toBin())

                val returnTypeList = listOf(RISCVGrammar.R_INSTR.InstrType.JALR, RISCVGrammar.R_INSTR.InstrType.JAL)

                while (result != null && !returnTypeList.contains(result.type)) {
                    result.type.execute(this, result.binaryMap)

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
            val binMapper = RISCVBinMapper()
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
                val destAddr = MutVal.Value.Hex(destAddrString)
                getConsole().info("--exe_until_line executing until line ${closestID + 1} or address ${destAddr.getHexStr()}")
                val measuredTime = measureTime {
                    while (instrCount < 1000) {
                        val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                        val result = binMapper.getInstrFromBinary(binary.get().toBin())
                        if (destAddr == getRegisterContainer().pc.value.get()) {
                            break
                        }
                        if (result != null) {
                            result.type.execute(this, result.binaryMap)
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
                            result.type.execute(this, result.binaryMap)
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

    override fun exeUntilAddress(address: MutVal.Value.Hex) {
        super.exeUntilAddress(address)
        var instrCount = 0
        val binMapper = RISCVBinMapper()
        getConsole().info("--exe_until_line executing until address ${address.getHexStr()}")
        val measuredTime = measureTime {
            while (instrCount < 1000) {
                val binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                val result = binMapper.getInstrFromBinary(binary.get().toBin())
                if (address == getRegisterContainer().pc.value.get()) {
                    break
                }
                if (result != null) {
                    result.type.execute(this, result.binaryMap)
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