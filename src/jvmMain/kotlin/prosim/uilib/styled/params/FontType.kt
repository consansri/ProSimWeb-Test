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

    fun getSkiaFont(): org.jetbrains.skia.Font {
        val theme = UIStates.theme.get()
        val tf = when (this) {
            BASIC -> theme.textLaF.getBaseTypeface()
            CODE -> theme.codeLaF.getTF()
            DATA -> theme.codeLaF.getTF()
            TITLE -> theme.textLaF.getTitleTypeface()
        }

        val fontScale = UIStates.scale.get().fontScale
        val scale = when (this) {
            BASIC -> fontScale.textSize
            CODE -> fontScale.codeSize
            DATA -> fontScale.dataSize
            TITLE -> fontScale.titleSize
        }
        return org.jetbrains.skia.Font(tf, scale * 1.7f)
    }
}