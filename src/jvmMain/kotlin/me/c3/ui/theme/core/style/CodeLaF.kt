package me.c3.ui.theme.core.style

import emulator.kit.assembler.CodeStyle
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class CodeLaF(
    font: Font,
    val pcIdenticator: String,
    val selectionColor: Color,
    val searchResultColor: Color,
    val getColor: (CodeStyle?) -> Color
) {
    private var font: Font = font
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }
    fun getFont(): Font = font

}