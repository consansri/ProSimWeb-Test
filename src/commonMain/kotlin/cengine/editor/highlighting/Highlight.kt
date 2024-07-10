package cengine.editor.highlighting

import cengine.lang.Language

data class Highlight(val range: IntRange, val type: Type) {
    fun color(language: Language?): Int? = language?.hlToColor(type)

    enum class Type {
        KEYWORD,
        FUNCTION,
        VARIABLE,
        STRING,
        COMMENT
    }
}
