package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder
import cengine.util.ByteBuffer

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?
    val labelDependent: Boolean

    /**
     * Will be called by [ELFBuilder] to build or reserve the instruction binary representation.
     *
     * Expressions: Local symbols will be linked but not labels.
     *
     * @return Binary Representation of the instruction (this can be overriden by [lateEvaluation])
     *
     *
     */
    fun build(instr: ASNode.Instruction): ByteBuffer

    /**
     * Will only be called by [ELFBuilder] if type is [labelDependent].
     *
     * Expressions: Local symbols and labels will be linked.
     *
     * @return Binary Representation of the instruction (this overrides previous binaries)
     */
    fun lateEvaluation(instrDef: ELFBuilder.Section.InstrDef): ByteBuffer
}