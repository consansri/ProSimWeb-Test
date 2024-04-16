package me.c3.ui.theme.core.style

import emulator.kit.assembly.Compiler
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class CodeLaF(
    font: Font,
    val pcIdenticator: String,
    val selectionColor: Color,
    val getColor: (Compiler.CodeStyle?) -> Color
) {
    private var font: Font = font
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }
    fun getFont(): Font = font

}