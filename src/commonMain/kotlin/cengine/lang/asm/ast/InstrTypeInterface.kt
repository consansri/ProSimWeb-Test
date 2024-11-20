package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?

    /**
     * Will be called by [AsmCodeGenerator] to build or reserve the instruction binary representation.
     *
     * Expressions: Local symbols will be linked but not labels.
     *
     * Insert Binary Representation of the instruction (this can be overriden by [lateEvaluation])
     * Queue [lateEvaluation] if local linked labels are needed.
     *
     */
    fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction)

    /**
     * Will only be called by [AsmCodeGenerator] if type reserved space in section at [resolve].
     *
     * Expressions: Local symbols and labels will be linked. (Relocations for intersectional label references needed)
     *
     * Replace Binary Representation of the instruction in [section] at [index] (this overrides previous binaries)
     */
    fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int)

}