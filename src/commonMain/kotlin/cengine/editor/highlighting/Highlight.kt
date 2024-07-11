package cengine.editor.highlighting

import cengine.lang.LanguageService

data class Highlight(val range: IntRange, val type: Type) {
    fun color(languageService: LanguageService?): Int? = languageService?.hlToColor(type)

    enum class Type {
        KEYWORD,
        FUNCTION,
        VARIABLE,
        STRING,
        COMMENT
    }
}
