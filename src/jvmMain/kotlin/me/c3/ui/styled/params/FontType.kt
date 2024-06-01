package me.c3.ui.styled.params

import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(): Font {
        return when (this) {
            BASIC -> ThemeManager.curr.textLaF.getBaseFont().deriveFont(ScaleManager.curr.fontScale.textSize)
            CODE -> ThemeManager.curr.codeLaF.getFont().deriveFont(ScaleManager.curr.fontScale.codeSize)
            DATA -> ThemeManager.curr.codeLaF.getFont().deriveFont(ScaleManager.curr.fontScale.dataSize)
            TITLE -> ThemeManager.curr.textLaF.getTitleFont().deriveFont(ScaleManager.curr.fontScale.titleSize)
        }
    }
}