package me.c3.ui.styled.params

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(themeManager: ThemeManager, scaleManager: ScaleManager): Font {
        return when (this) {
            BASIC -> themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
            CODE -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)
            DATA -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)
            TITLE -> themeManager.curr.textLaF.getTitleFont().deriveFont(scaleManager.curr.fontScale.titleSize)
        }
    }
}