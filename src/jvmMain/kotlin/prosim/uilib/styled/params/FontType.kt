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

}