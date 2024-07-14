package cengine.editor.selection

import cengine.editor.text.TextModel

data class Caret(
    val model: TextModel
) {
    var index: Int = 0
        set(value) {
            field = value
            val res = model.getLineAndColumn(value)
            line = res.first
            col = res.second
        }

    var line: Int = 0
    var col: Int = 0

    fun set(line: Int, col: Int) {
        index = model.indexOf(line, col)
    }

    fun set(newIndex: Int) {
        index = newIndex
    }

    fun moveUp(offset: Int) {
        if (line - offset >= 0) {
            index = model.indexOf(line - offset, col)
        }else{
            index = 0
        }
    }

    fun moveDown(offset: Int) {
        if (line + offset <= model.lines) {
            index = model.indexOf(line + offset, col)
        }
    }

    operator fun plusAssign(other: Int) {
        if (index + other <= model.length) {
            index += other
        }
    }

    operator fun minusAssign(other: Int) {
        if (index - other >= 0) {
            index -= other
        }
    }
}