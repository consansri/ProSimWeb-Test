package prosim.uilib.styled.params

import prosim.uilib.UIStates
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    DATA,
    TITLE;

    fun getFont(): Font {
        return when (this) {
            BASIC -> UIStates.theme.get().textLaF.getBaseFont().deriveFont(UIStates.scale.get().fontScale.textSize)
            CODE -> UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.codeSize)
            DATA -> UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.dataSize)
            TITLE -> UIStates.theme.get().textLaF.getTitleFont().deriveFont(UIStates.scale.get().fontScale.titleSize)
        }
    }
}