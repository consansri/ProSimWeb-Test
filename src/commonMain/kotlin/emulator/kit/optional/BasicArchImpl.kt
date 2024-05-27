package emulator.kit.optional

import Performance
import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import kotlin.time.measureTime

abstract class BasicArchImpl(config: Config, asmConfig: AsmConfig) : emulator.kit.Architecture(config, asmConfig) {
    override fun exeContinuous() {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeContinuous()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= Performance.MAX_INSTR_EXE_AMOUNT) {
                instrCount++
                result = executeNext()
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("continuous \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeSingleStep() {
        val measuredTime = measureTime {
            super.exeSingleStep() // clears console
            executeNext()
        }

        Performance.updateExePerformance(1, measuredTime)

        getConsole().exeInfo("single step \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeMultiStep(steps: Long) {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeMultiStep(steps)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount < steps) {
                instrCount++
                result = executeNext()
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("$steps steps \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeSkipSubroutine() {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeSkipSubroutine()

            var result = executeNext()
            instrCount++

            if (result.valid && result.typeIsBranchToSubroutine) {
                while (result.valid && instrCount <= 1000) {
                    instrCount++
                    result = executeNext()
                    if (result.typeIsReturnFromSubroutine) {
                        break
                    }
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("skip subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeReturnFromSubroutine() {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeReturnFromSubroutine()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext()
                if (result.typeIsReturnFromSubroutine) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("return from subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeUntilAddress(address: Variable.Value.Hex) {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeUntilAddress(address)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext()
                if (getRegContainer().pc.get().toHex().getRawHexStr().uppercase() == address.getRawHexStr().uppercase()) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("until $address \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeUntilLine(lineID: Int, wsRelativeFileName: String) {
        var instrCount = 0L
        val measuredTime = measureTime {
            super.exeUntilLine(lineID, wsRelativeFileName)

            val lineAddressMap = getAssembler().lastInvertedLineMap(wsRelativeFileName).toList()

            var closestID: Int? = null
            for (entry in lineAddressMap) {
                if (entry.first.lineID >= lineID && entry.first.lineID != closestID) {
                    if (closestID != null) {
                        if (entry.first.lineID < closestID) {
                            closestID = entry.first.lineID
                        }
                    } else {
                        closestID = entry.first.lineID
                    }
                }
                if (closestID == lineID) break
            }
            val destAddrString = lineAddressMap.map { it.first.lineID to it.second }.firstOrNull { it.first == closestID }?.second ?: ""

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext()
                if (getRegContainer().pc.get().toHex().getRawHexStr().uppercase() == destAddrString.uppercase()) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        getConsole().exeInfo("until line ${lineID + 1} \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }


    abstract fun executeNext(): ExecutionResult


    data class ExecutionResult(val valid: Boolean, val typeIsReturnFromSubroutine: Boolean, val typeIsBranchToSubroutine: Boolean)


}