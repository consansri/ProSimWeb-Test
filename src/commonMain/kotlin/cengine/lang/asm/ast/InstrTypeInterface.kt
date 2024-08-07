package cengine.lang.asm.ast

import cengine.lang.asm.parser.Rule

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    fun getDetectionName(): String
}