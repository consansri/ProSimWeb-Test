package cengine.editor.annotation

import cengine.lang.asm.CodeStyle

enum class Severity(val color: CodeStyle?) {
    INFO(null),
    WARNING(CodeStyle.YELLOW),
    ERROR(CodeStyle.RED);

    fun toColor(): Int? = color?.getDarkElseLight()
}