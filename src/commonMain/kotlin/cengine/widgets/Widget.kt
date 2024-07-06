package cengine.widgets

data class Widget(
    val id: String,
    val type: WidgetType,
    val content: String,
    var startIndex: Int,
    var endIndex: Int
) {

    enum class WidgetType{
        INTERLINE,
        POSTLINE,
        INLAY
    }
}