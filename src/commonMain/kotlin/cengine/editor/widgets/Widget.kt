package cengine.editor.widgets


import emulator.kit.nativeLog

data class Widget(
    val id: String,
    val content: String,
    val type: Type,
    val position: Int,
    val onClick: () -> Unit = {
        nativeLog("Click on Widget: $id")
    }
) {
    enum class Type{
        INTERLINE,
        INLAY,
        POSTLINE
    }
}