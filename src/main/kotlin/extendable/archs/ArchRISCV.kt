package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVAssembly
import extendable.archs.riscv.RISCVBinMapper
import extendable.archs.riscv.RISCVGrammar
import extendable.components.assembly.Compiler
import extendable.components.types.ByteValue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

class ArchRISCV() : Architecture(RISCV.config, RISCV.asmConfig) {

    @OptIn(ExperimentalTime::class)
    override fun exeContinuous() {
        var instrCount = 0
        val measuredTime = measureTime {
            super.exeContinuous()

            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())

            while (result != null && instrCount < 5000) {
                instrCount++
                result.type.execute(this, result.binaryMap)

                // Load next Instruction
                binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
                result = binMapper.getInstrFromBinary(binary.get().toBin())
            }
        }

        getConsole().info("finish --continuous in ${measuredTime.inWholeMilliseconds} ms [executed $instrCount instructions]")

    }

    @OptIn(ExperimentalTime::class)
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

        getConsole().info("finish --single_step in ${measuredTime.inWholeMilliseconds} ms")

    }

    @OptIn(ExperimentalTime::class)
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

        getConsole().info("finish --multi_step in ${measuredTime.inWholeMilliseconds} ms [executed $instrCount instructions]")
    }

    @OptIn(ExperimentalTime::class)
    override fun exeSkipSubroutines() {

        val measuredTime = measureTime {
            super.exeSkipSubroutines()
            val binMapper = RISCVBinMapper()
            var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            var result = binMapper.getInstrFromBinary(binary.get().toBin())
            if (result != null) {
                if (result.type == RISCVGrammar.T1Instr.Type.JAL || result.type == RISCVGrammar.T1Instr.Type.JALR) {
                    getRegisterContainer().pc.value.set(getRegisterContainer().pc.value.get() + ByteValue.Type.Hex("4"))
                } else {
                    result.type.execute(this, result.binaryMap)
                }
            }
        }

        getConsole().info("finish --skip_subroutine in ${measuredTime.inWholeMilliseconds} ms")
    }

    @OptIn(ExperimentalTime::class)
    override fun exeSubroutine() {
        super.exeSubroutine()

    }

    @OptIn(ExperimentalTime::class)
    override fun exeClear() {
        super.exeClear()

    }

    override fun getPreHighlighting(line: String): String {

        return super.getPreHighlighting(line)
    }

    override fun hlAndCompile(code: String, startAtLine: Int): Compiler.CompilationResult {

        /* ----------------------- Token Identification ------------------------- */
        /**
         *   Line by Line
         *   1. Find Assembly Tokens
         *   2. Highlight Assembly Tokens
         */


        /* ----------------------- Highlight Tokens ------------------------- */

        //  HL Assembly Tokens


        /* ------------------------------------------------------------------------------------------------------- */


        /* ----------------------- Generate Disassembled View and Write Binary to Memory ------------------------- */
        /**
         *   Line by Line
         */

        if (true) {
            /* ------------------------- Generate Transcript --------------------------- */


            /* ----------------------- Write Binary to Memory -------------------------- */

        }
        return Compiler.CompilationResult(getAssembly().getHLContent(), getAssembly().isBuildable())

    }
}