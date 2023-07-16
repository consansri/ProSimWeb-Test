package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVBinMapper
import extendable.archs.riscv.RISCVGrammar
import extendable.components.types.MutVal
import tools.DebugTools
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ArchRISCV() : Architecture(RISCV.config, RISCV.asmConfig) {

    override fun exeContinuous() {
        var instrCount = 0
        val measuredTime = measureTime {
            super.exeContinuous()

            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())

            while (result != null && instrCount < 10000) {
                instrCount++
                result.type.execute(this, result.binaryMap)

                // Load next Instruction
                binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                result = binMapper.getInstrFromBinary(binary.get().toBin())
            }
        }

        getConsole().log("--continuous finishing... \ntook ${measuredTime.inWholeMilliseconds} ms [executed $instrCount instructions]")

    }

    override fun exeSingleStep() {

        val measuredTime = measureTime {
            super.exeSingleStep()

            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())
            if (result != null) {
                result.type.execute(this, result.binaryMap)
            }
        }

        DebugTools.testBinTools(getConsole())

        getConsole().log("--single_step finishing... \ntook ${measuredTime.inWholeMilliseconds} ms")

    }

    override fun exeMultiStep(steps: Int) {
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

        getConsole().log("--multi_step finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMilliseconds} ms")
    }

    override fun exeSkipSubroutine() {
        var instrCount = 0

        val measuredTime = measureTime {
            super.exeSkipSubroutine()
            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())
            if (result != null) {
                if (result.type == RISCVGrammar.T1Instr.Type.JAL || result.type == RISCVGrammar.T1Instr.Type.JALR) {
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

        getConsole().log("--exe_skip_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMilliseconds} ms")
    }

    override fun exeReturnFromSubroutine() {
        super.exeReturnFromSubroutine()
        var instrCount = 0
        val measuredTime = measureTime {
            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())

            val returnTypeList = listOf(RISCVGrammar.T1Instr.Type.JALR, RISCVGrammar.T1Instr.Type.JAL)

            while (result != null && !returnTypeList.contains(result.type)) {
                result.type.execute(this, result.binaryMap)

                binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                result = binMapper.getInstrFromBinary(binary.get().toBin())
                instrCount++
            }
        }

        getConsole().log("--exe_return_from_subroutine finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMilliseconds} ms")

    }

    override fun exeUntilLine(lineID: Int) {
        super.exeUntilLine(lineID)
        var instrCount = 0
        val binMapper = RISCVBinMapper()
        val lineAddressMap = getAssembly().getAssemblyMap().lineAddressMap.map { it.value to it.key }
        var closestID: Int? = null
        for (entry in lineAddressMap) {
            if (entry.first > lineID) {
                if (closestID != null) {
                    if (entry.first < closestID) {
                        closestID = entry.first
                    }
                } else {
                    closestID = entry.first
                }
            }
        }

        if (closestID != null) {
            val destAddr = MutVal.Value.Hex(lineAddressMap.get(closestID).second)
            getConsole().info("--exe_until_line executing until line ${closestID + 1} or address ${destAddr.getHexStr()}")
            val measuredTime = measureTime {
                super.exeSkipSubroutine()

                while (instrCount < 10000) {
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

            getConsole().log("--exe_until_line finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMilliseconds} ms")
        } else {
            getConsole().info("--exe_continuous")

            val measuredTime = measureTime {
                while (true) {
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

            getConsole().log("--exe_continuous finishing... \nexecuting $instrCount instructions took ${measuredTime.inWholeMilliseconds} ms")
        }


    }

    override fun exeClear() {
        super.exeClear()

    }

}