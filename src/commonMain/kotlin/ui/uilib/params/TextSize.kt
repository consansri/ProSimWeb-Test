package ui.uilib.params

import androidx.compose.ui.unit.TextUnit
import ui.uilib.UIState

enum class TextSize {
    SMALL,
    MEDIUM,
    LARGE;


    fun get(): TextUnit {
        val scale = UIState.Scale.value
        return when(this){
            SMALL -> scale.FONTSCALE_SMALL
            MEDIUM -> scale.FONTSCALE_MEDIUM
            LARGE -> scale.FONTSCALE_LARGE
        }
    }

}