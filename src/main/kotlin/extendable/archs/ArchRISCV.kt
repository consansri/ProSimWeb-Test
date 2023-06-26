package extendable.cisc

import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.archs.riscv.RISCVFlags
import extendable.components.assembly.Assembly
import extendable.components.connected.Instruction
import extendable.components.types.ByteValue

class ArchRISCV() : Architecture(RISCV.config) {

    override fun exeContinuous() {
        super.exeContinuous()
        val reg = getRegisterContainer().getRegister("a0")

        reg?.let {
            reg.set(reg.get() + ByteValue.Type.Dec("1", reg.byteValue.size))
        }

        for (i in 0..100) {
            getMemory().saveDec(i.toDouble(), i.toString(10))
        }
        for (ins in getInstructions()) {
            if (ins.name == "ADD") {
                getConsole().log("execute ADD")
                val reg1 = getRegisterContainer().getRegister("ra")
                reg1?.let {
                    ins.execute(this, Instruction.ExecutionMode.EXECUTION(emptyList()))
                }
            }
        }
    }

    override fun exeMultiStep(steps: Int) {
        super.exeMultiStep(steps)
        getMemory().saveDec(0.0, steps.toString(10))
    }

    override fun exeClear() {
        super.exeClear()

    }

    override fun getPreHighlighting(line: String): String {


        return super.getPreHighlighting(line)
    }

    override fun hlAndCompile(code: String, startAtLine: Int): Assembly.CompilationResult {

        val assembly = Assembly(
            this,
            code,
            Assembly.RegexCollection(
                Regex("""^\s+"""),
                Regex("""^[^0-9A-Za-z]"""),
                Regex("""^${ArchConst.PRESTRING_BINARY}[01]+"""),
                Regex("""^${ArchConst.PRESTRING_HEX}[0-9a-f]+""", RegexOption.IGNORE_CASE),
                Regex("""^${ArchConst.PRESTRING_DECIMAL}-[0-9]+"""),
                Regex("""^${ArchConst.PRESTRING_DECIMAL}[0-9]+"""),
                Regex("""^[a-z][a-z0-9]*""", RegexOption.IGNORE_CASE),
                Regex("""^[a-z]+""", RegexOption.IGNORE_CASE)
            ),
            Assembly.HLFlagCollection(
                RISCVFlags.keyword,
                RISCVFlags.identifier,
                RISCVFlags.hex,
                RISCVFlags.bin,
                RISCVFlags.dec,
                RISCVFlags.udec,
                RISCVFlags.register,
                RISCVFlags.symbol,
                RISCVFlags.instruction,
                RISCVFlags.comment
            ),
            Assembly.AssemblyOption.OptSplitParam(",")
        )

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

        return Assembly.CompilationResult(assembly.getHLContent(), true)
    }
}