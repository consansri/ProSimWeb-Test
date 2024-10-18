package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.obj.elf.ELFBuilder

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?

    /**
     * Will be called by [ELFBuilder] to build or reserve the instruction binary representation.
     *
     * Expressions: Local symbols will be linked but not labels.
     *
     * Insert Binary Representation of the instruction (this can be overriden by [lateExecutableEval])
     * Queue [lateExecutableEval] if local linked labels are needed.
     *
     */
    fun resolve(builder: ELFBuilder, instr: ASNode.Instruction)

    /**
     * Will only be called by [ELFBuilder] if type reserved space in section at [resolve].
     *
     * Expressions: Local symbols and labels will be linked. (Relocations for intersectional label references needed)
     *
     * Replace Binary Representation of the instruction in [section] at [index] (this overrides previous binaries)
     */
    fun lateEvaluation(builder: ELFBuilder, section: ELFBuilder.Section, instr: ASNode.Instruction, index: Int)

}