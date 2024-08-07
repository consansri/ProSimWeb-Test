package cengine.editor.widgets


import emulator.kit.nativeLog

data class Widget(
    val id: String,
    val content: String,
    val type: Type,
    private val indexProvider: () -> Int,
    val onClick: () -> Unit = {
        nativeLog("Click on Widget: $id")
    }
) {
    val index: Int
        get() = indexProvider()

    enum class Type{
        INTERLINE,
        INLAY,
        POSTLINE
    }

    override fun toString(): String {
        return "{$id $content $type $index}"
    }
}