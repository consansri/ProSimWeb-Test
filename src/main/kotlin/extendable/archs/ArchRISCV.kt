package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVAssembly
import extendable.archs.riscv.RISCVBinMapper
import extendable.components.assembly.Compiler
import extendable.components.types.ByteValue

class ArchRISCV() : Architecture(RISCV.config, RISCV.asmConfig) {

    override fun exeContinuous() {
        super.exeContinuous()

        val binMapper = RISCVBinMapper()
        var binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
        var result = binMapper.getInstrFromBinary(binary.get().toBin())
        while (result != null) {

            result.type.execute(this, result.binaryMap)

            // Load next Instruction
            binary = getMemory().load(getRegisterContainer().pc.value.get(), 4)
            result = binMapper.getInstrFromBinary(binary.get().toBin())
        }


    }

    override fun exeMultiStep(steps: Int) {
        super.exeMultiStep(steps)
        if (getAssembly().isBuildable()) {


        }
    }

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