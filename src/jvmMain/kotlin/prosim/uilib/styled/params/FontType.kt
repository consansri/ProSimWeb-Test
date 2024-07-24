package prosim.uilib.styled.params

import prosim.uilib.UIStates
import java.awt.Font

enum class FontType {
    BASIC,
    CODE,
    CODE_INFO,
    DATA,
    TITLE;

    fun getFont(): Font {
        return when (this) {
            BASIC -> UIStates.scale.get().FONT_TEXT_MEDIUM
            CODE -> UIStates.scale.get().FONT_CODE_MEDIUM
            DATA -> UIStates.scale.get().FONT_CODE_MEDIUM
            TITLE -> UIStates.scale.get().FONT_TEXT_LARGE
            CODE_INFO -> UIStates.scale.get().FONT_CODE_SMALL
        }
    }

    fun getSkiaFont(): org.jetbrains.skia.Font {
        val scale = UIStates.scale.get()
        val tf = when (this) {
            BASIC -> scale.TF_TEXT
            CODE -> scale.TF_CODE
            DATA -> scale.TF_CODE
            TITLE -> scale.TF_TEXT
            CODE_INFO -> scale.TF_CODE
        }

        val fontScale = when (this) {
            BASIC -> scale.FONTSCALE_MEDIUM
            CODE -> scale.FONTSCALE_MEDIUM
            DATA -> scale.FONTSCALE_MEDIUM
            TITLE -> scale.FONTSCALE_LARGE
            CODE_INFO -> scale.FONTSCALE_SMALL * 0.002f
        }
        return org.jetbrains.skia.Font(tf, fontScale * 1.7f)
    }
}