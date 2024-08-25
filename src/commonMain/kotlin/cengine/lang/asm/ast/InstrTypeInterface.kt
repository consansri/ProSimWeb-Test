package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.RelocatableELFBuilder

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?

    /**
     * Will be called by [RelocatableELFBuilder] to build or reserve the instruction binary representation.
     *
     * Expressions: Local symbols will be linked but not labels.
     *
     * Insert Binary Representation of the instruction (this can be overriden by [lateEvaluation])
     * Queue [lateEvaluation] if local linked labels are needed.
     *
     */
    fun resolve(builder: RelocatableELFBuilder, instr: ASNode.Instruction)

    /**
     * Will only be called by [RelocatableELFBuilder] if type reserved space in section at [resolve].
     *
     * Expressions: Local symbols and labels will be linked.
     *
     * Replace Binary Representation of the instruction in [section] at [index] (this overrides previous binaries)
     */
    fun lateEvaluation(builder: RelocatableELFBuilder, section: RelocatableELFBuilder.Section, instr: ASNode.Instruction, index: Int)
}