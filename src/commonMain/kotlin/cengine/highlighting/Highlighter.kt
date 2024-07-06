package cengine.highlighting

import cengine.text.TextModel

interface Highlighter {
    fun updateHL(model: TextModel)
    fun getHighlighting(index: Int): Style?
}