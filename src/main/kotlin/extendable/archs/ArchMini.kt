package extendable.cisc

import extendable.Architecture
import extendable.archs.mini.Mini
import extendable.components.assembly.Compiler

class ArchMini : Architecture {

    constructor() : super(Mini.config, Mini.asmConfig) {

    }

    override fun exeContinuous() {
        super.exeContinuous()
        val flag = getFlagsConditions()?.findFlag("Carry")
        if (flag != null) {
            getFlagsConditions()?.setFlag(flag, !flag.getValue())
            console.log("Flag ${flag?.name} ${flag?.getValue()}")
        }




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

        return Compiler.CompilationResult(getAssembly().getHLContent(), true)
    }

}