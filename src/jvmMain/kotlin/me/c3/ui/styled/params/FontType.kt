package me.c3.ui.styled.params

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(tm: ThemeManager, sm: ScaleManager): Font {
        return when (this) {
            BASIC -> tm.curr.textLaF.getBaseFont().deriveFont(sm.curr.fontScale.textSize)
            CODE -> tm.curr.codeLaF.getFont().deriveFont(sm.curr.fontScale.codeSize)
            DATA -> tm.curr.codeLaF.getFont().deriveFont(sm.curr.fontScale.dataSize)
            TITLE -> tm.curr.textLaF.getTitleFont().deriveFont(sm.curr.fontScale.titleSize)
        }
    }
}