package emulator.kit.assembly.standards

import emulator.kit.configs.AsmConfig
import emulator.kit.configs.Config
import emulator.kit.types.Variable
import kotlin.time.measureTime

abstract class StandardArch(config: Config, asmConfig: AsmConfig) : emulator.kit.Architecture(config, asmConfig) {


    override fun exeContinuous() {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
        val measuredTime = measureTime {
            super.exeContinuous()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext()
            }
        }
        getConsole().exeInfo("continuous \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeSingleStep() {
        if (!this.getCompiler().isBuildable()) return

        val measuredTime = measureTime {
            super.exeSingleStep() // clears console
            executeNext()
        }

        getConsole().exeInfo("single step \ntook ${measuredTime.inWholeMicroseconds} μs")
    }

    override fun exeMultiStep(steps: Int) {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
        val measuredTime = measureTime {
            super.exeMultiStep(steps)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount < steps) {
                instrCount++
                result = executeNext()
            }
        }
        getConsole().exeInfo("$steps steps \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeSkipSubroutine() {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
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
        getConsole().exeInfo("skip subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeReturnFromSubroutine() {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
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
        getConsole().exeInfo("return from subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeUntilAddress(address: Variable.Value.Hex) {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
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
        getConsole().exeInfo("until $address \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }

    override fun exeUntilLine(lineID: Int, fileName: String) {
        if (!this.getCompiler().isBuildable()) return
        var instrCount = 0
        val measuredTime = measureTime {
            super.exeUntilLine(lineID, fileName)

            val lineAddressMap = getCompiler().getAssemblyMap().lineAddressMap.map { it.value to it.key }.filter { it.first.fileName == fileName }
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
        getConsole().exeInfo("until line ${lineID + 1} \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]")
    }


    abstract fun executeNext(): ExecutionResult


    data class ExecutionResult(val valid: Boolean, val typeIsReturnFromSubroutine: Boolean, val typeIsBranchToSubroutine: Boolean)


}