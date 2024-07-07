package cengine.editor.highlighting

import cengine.editor.text.TextModel

interface Highlighter {
    fun updateHL(model: TextModel)
    fun getHighlighting(index: Int): Style?
}