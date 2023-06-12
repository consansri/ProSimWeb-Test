package extendable.components.types

sealed class ExtensionType

data class TypeBIN(val binary: String) : ExtensionType()
data class TypeHEX(val hex: String) : ExtensionType()
data class TypeDEC(val long: Long) : ExtensionType()
data class TypeLABEL(val label: String) : ExtensionType()
