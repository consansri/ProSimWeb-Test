package cengine.editor.widgets


import cengine.psi.core.TextPosition

data class Widget(
    val id: String,
    val content: String,
    val type: Type,
    val position: TextPosition,
    val onClick: () -> Unit = {}
) {
    enum class Type{
        INTERLINE,
        INLAY,
        POSTLINE
    }
}