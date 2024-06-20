package me.c3.uilib.styled.params

import me.c3.uilib.UIManager
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(): Font {
        return when (this) {
            BASIC -> UIManager.theme.get().textLaF.getBaseFont().deriveFont(UIManager.scale.get().fontScale.textSize)
            CODE -> UIManager.theme.get().codeLaF.getFont().deriveFont(UIManager.scale.get().fontScale.codeSize)
            DATA -> UIManager.theme.get().codeLaF.getFont().deriveFont(UIManager.scale.get().fontScale.dataSize)
            TITLE -> UIManager.theme.get().textLaF.getTitleFont().deriveFont(UIManager.scale.get().fontScale.titleSize)
        }
    }
}