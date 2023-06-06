package extendable.components.types

sealed class ExtensionType

data class TypeAddr(val addr: Double) : ExtensionType()
data class TypeCSR(val csr: String) : ExtensionType()
data class TypeReg(val name: String) : ExtensionType()
data class TypeImm(val value: Int) : ExtensionType()
data class TypeJAddr(val jumpAddr: Double) : ExtensionType()
data class TypeShift(val shift: Int) : ExtensionType()
