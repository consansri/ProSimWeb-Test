package cengine.lang.asm.ast

import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.target.ikrmini.IKRMiniSpec
import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Spec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.lang.asm.ast.target.riscv.rv64.RV64Spec
import cengine.lang.asm.ast.target.t6502.T6502Spec
import emulator.core.Size

/**
 * Interface representing a defined assembly configuration.
 */
interface AsmSpec {
    companion object {
        val specs = setOf(RV32Spec, RV64Spec, IKRR2Spec, IKRMiniSpec, T6502Spec)
    }

    val name: String

    /** The size of memory addresses. */
    val memAddrSize: Size

    /** The size of words. */
    val wordSize: Size

    /** Determines if registers are detected by name. */
    val detectRegistersByName: Boolean

    /** The lexer prefices used for parsing instructions and directives. */
    val prefices: AsmLexer.Prefices

    val allRegs: List<RegTypeInterface>

    val allInstrs: List<InstrTypeInterface>

    val customDirs: List<DirTypeInterface>

    fun createLexer(input: String): AsmLexer = AsmLexer(input, this)

    override fun toString(): String
}