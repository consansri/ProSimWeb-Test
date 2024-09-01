package ui.uilib.params

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.compose.resources.Font
import ui.uilib.UIState

enum class FontType {
    CODE,
    SMALL,
    MEDIUM,
    LARGE;

    fun getSize(): TextUnit {
        val scale = UIState.Scale.value
        return when (this) {
            SMALL -> scale.FONTSCALE_SMALL
            MEDIUM -> scale.FONTSCALE_MEDIUM
            LARGE -> scale.FONTSCALE_LARGE
            CODE -> scale.FONTSCALE_MEDIUM
        }
    }

    @Composable
    fun getFamily(): FontFamily{
        val theme = UIState.Theme.value
        return FontFamily(
            when(this){
                CODE -> Font(theme.FONT_CODE)
                else -> Font(theme.FONT_BASIC)
            }
        )
    }

}