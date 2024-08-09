package cengine.lang.asm.ast

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val isPseudo: Boolean
    val bytesNeeded: Int?
    fun getDetectionName(): String
}