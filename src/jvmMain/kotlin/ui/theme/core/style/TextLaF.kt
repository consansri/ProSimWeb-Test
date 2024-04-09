package me.c3.ui.theme.core.style

import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class TextLaF(
    val base: Color,
    val baseSecondary: Color,
    font: Font,
    titleFont: Font
){
    private var font: Font = font
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }

    private var titleFont: Font = titleFont
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }

    fun getBaseFont(): Font = font
    fun getTitleFont(): Font = titleFont

}