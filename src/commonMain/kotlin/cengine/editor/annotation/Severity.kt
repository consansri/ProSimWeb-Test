package cengine.editor.annotation

import cengine.lang.LanguageService

enum class Severity {
    INFO,
    WARNING,
    ERROR;

    fun toColor(languageService: LanguageService?): Int? = languageService?.severityToColor(this)
}