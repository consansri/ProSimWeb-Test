package me.c3.uilib.styled.params

import me.c3.ui.States
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(): Font {
        return when (this) {
            BASIC -> States.theme.get().textLaF.getBaseFont().deriveFont(States.scale.get().fontScale.textSize)
            CODE -> States.theme.get().codeLaF.getFont().deriveFont(States.scale.get().fontScale.codeSize)
            DATA -> States.theme.get().codeLaF.getFont().deriveFont(States.scale.get().fontScale.dataSize)
            TITLE -> States.theme.get().textLaF.getTitleFont().deriveFont(States.scale.get().fontScale.titleSize)
        }
    }
}