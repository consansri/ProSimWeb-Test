package cengine.lang.asm.ast

import cengine.lang.asm.parser.Rule

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val isPseudo: Boolean
    val bytesNeeded: Int?
    fun getDetectionName(): String
}