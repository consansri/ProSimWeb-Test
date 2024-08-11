package cengine.lang.asm.ast

interface InstrTypeInterface {
    val typeName: String
    val paramRule: Rule?
    val bytesNeeded: Int?
    val detectionName: String
    val inCodeInfo: String?
}