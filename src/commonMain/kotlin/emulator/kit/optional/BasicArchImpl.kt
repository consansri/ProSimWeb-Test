package emulator.kit.optional

import Performance
import cengine.util.integer.Hex
import emulator.kit.config.AsmConfig
import emulator.kit.config.Config
import emulator.kit.memory.Memory
import kotlin.time.measureTime

abstract class BasicArchImpl(config: Config, asmConfig: AsmConfig) : emulator.kit.Architecture(config, asmConfig) {
    override fun exeContinuous() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeContinuous()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= Performance.MAX_INSTR_EXE_AMOUNT) {
                instrCount++
                result = executeNext(tracker)
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("continuous \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeSingleStep() {
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeSingleStep() // clears console
            executeNext(tracker)
        }

        Performance.updateExePerformance(1, measuredTime)

        console.exeInfo("single step \ntook ${measuredTime.inWholeMicroseconds} μs\n$tracker")
    }

    override fun exeMultiStep(steps: Long) {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeMultiStep(steps)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount < steps) {
                instrCount++
                result = executeNext(tracker)
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("$steps steps \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeSkipSubroutine() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeSkipSubroutine()

            var result = executeNext(tracker)
            instrCount++

            if (result.valid && result.typeIsBranchToSubroutine) {
                while (result.valid && instrCount <= 1000) {
                    instrCount++
                    result = executeNext(tracker)
                    if (result.typeIsReturnFromSubroutine) {
                        break
                    }
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("skip subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeReturnFromSubroutine() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeReturnFromSubroutine()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext(tracker)
                if (result.typeIsReturnFromSubroutine) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("return from subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeUntilAddress(address: Hex) {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeUntilAddress(address)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext(tracker)
                if (regContainer.pc.get().toHex().rawInput.uppercase() == address.rawInput.uppercase()) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("until $address \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeUntilLine(lineID: Int, wsRelativeFileName: String) {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeUntilLine(lineID, wsRelativeFileName)

            val lineAddressMap = assembler.lastInvertedLineMap(wsRelativeFileName).toList()

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
                result = executeNext(tracker)
                if (regContainer.pc.get().toHex().rawInput.uppercase() == destAddrString.uppercase()) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("until line ${lineID + 1} \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }


    abstract fun executeNext(tracker: Memory.AccessTracker): ExecutionResult


    data class ExecutionResult(val valid: Boolean, val typeIsReturnFromSubroutine: Boolean, val typeIsBranchToSubroutine: Boolean)



}