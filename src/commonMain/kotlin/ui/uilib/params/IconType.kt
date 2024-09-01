package ui.uilib.params

import androidx.compose.ui.unit.Dp
import kotlinx.serialization.builtins.UIntArraySerializer
import ui.uilib.UIState

enum class IconType {
    SMALL,
    MEDIUM;

    fun getSize(): Dp{
        val scale = UIState.Scale.value
        return when(this){
            SMALL -> scale.SIZE_CONTROL_SMALL
            MEDIUM -> scale.SIZE_CONTROL_MEDIUM
        }
    }
}