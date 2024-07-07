package cengine.editor.widgets

interface WidgetModel {
    fun insert(widget: Widget)
    fun findOverlapping(start: Int, end: Int): List<Widget>
    fun updateTextForEdit(editStart: Int, oldLength: Int, newLength: Int)
    fun applyLazyShift()
    fun queueLazyShift(delta: Int)
}