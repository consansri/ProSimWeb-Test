package cengine.lang.asm.ast

import cengine.lang.asm.lexer.AsmLexer
import emulator.core.Size

/**
 * Interface representing a defined assembly configuration.
 */
interface AsmSpec {
    /** The size of memory addresses. */
    val memAddrSize: Size

    /** The size of words. */
    val wordSize: Size

    /**
     * USHR Amount for calculating the true memory address. (0 is byte aligned memory addresses)
     */
    val addrShift: Int

    /** Determines if registers are detected by name. */
    val detectRegistersByName: Boolean

    /** The lexer prefices used for parsing instructions and directives. */
    val prefices: AsmLexer.Prefices

    val allRegs: List<RegTypeInterface>

    val allInstrs: List<InstrTypeInterface>

    val customDirs: List<DirTypeInterface>

    fun createLexer(input: String): AsmLexer = AsmLexer(input, this)
}