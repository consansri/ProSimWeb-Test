package extendable.cisc

import extendable.Architecture
import extendable.archs.riscii.RISCII
import extendable.components.assembly.Compiler

class ArchRISCII : Architecture {

    constructor() : super(RISCII.config, RISCII.asmConfig) {

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