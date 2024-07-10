package cengine.editor.annotation

import cengine.lang.Language

enum class Severity {
    INFO,
    WARNING,
    ERROR;

    fun toColor(language: Language?): Int? = language?.severityToColor(this)
}