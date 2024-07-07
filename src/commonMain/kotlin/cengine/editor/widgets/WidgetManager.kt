package cengine.editor.widgets

import cengine.editor.Clearable

class WidgetManager: Clearable {
    val interlineWidgets = mutableMapOf<Int, List<Widget>>()
    val inlayWidgets = mutableMapOf<Pair<Int, Int>, List<Widget>>()
    val postlineWidgets = mutableMapOf<Int, List<Widget>>()

    fun addInterlineWidget(line: Int, widget: Widget) {
        val currWidgets = interlineWidgets[line]
        interlineWidgets[line] = listOfNotNull(widget, *currWidgets?.toTypedArray() ?: arrayOf())
    }

    fun addInlayWidget(line: Int, column: Int, widget: Widget) {
        val currWidgets = inlayWidgets[line to column]
        inlayWidgets[line to column] = listOfNotNull(widget, *currWidgets?.toTypedArray() ?: arrayOf())
    }

    fun addPostlineWidget(line: Int, widget: Widget) {
        val currWidgets = postlineWidgets[line]
        postlineWidgets[line] = listOfNotNull(widget, *currWidgets?.toTypedArray() ?: arrayOf())
    }

    override fun clear() {
        interlineWidgets.clear()
        inlayWidgets.clear()
        postlineWidgets.clear()
    }
}