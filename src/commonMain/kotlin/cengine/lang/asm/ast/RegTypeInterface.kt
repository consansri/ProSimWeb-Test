package cengine.lang.asm.ast

interface RegTypeInterface {
    val name: String
    val displayName: String
        get() = name.lowercase()

    val recognizable: List<String>
}