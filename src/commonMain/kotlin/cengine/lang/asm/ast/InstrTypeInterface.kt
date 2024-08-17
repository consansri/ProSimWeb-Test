package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?
    fun execute(builder: ELFBuilder, instr: ASNode.Instruction)
    fun checkSemantic(instr: ASNode.Instruction)
}