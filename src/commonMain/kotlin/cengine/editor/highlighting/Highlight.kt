package cengine.editor.highlighting

import cengine.lang.asm.CodeStyle

data class Highlight(override val range: IntRange, val type: Type): HLInfo {

    override val color: Int
        get() = type.style.getDarkElseLight()

    enum class Type(val style: CodeStyle) {
        KEYWORD(CodeStyle.ORANGE),
        FUNCTION(CodeStyle.BLUE),
        IDENTIFIER(CodeStyle.MAGENTA),
        STRING(CodeStyle.ALTGREEN),
        COMMENT(CodeStyle.comment),
        NUMBER_LITERAL(CodeStyle.ALTBLUE)
    }
}
