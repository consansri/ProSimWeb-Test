package ui.uilib.scale

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Immutable
class Scaling(val scale: Float = 1f) {

    val name: String = "${(scale * 100).toInt()}%"

    // SOURCE PATHS
    val PATH_FONT_TEXT: String = "fonts/Roboto/Roboto-Regular.ttf"
    val PATH_FONT_CODE: String = "fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"

    // FONT SCALING
    val FONTSCALE_SMALL: TextUnit  = 0.7.em
        get() = (field * scale)
    val FONTSCALE_MEDIUM: TextUnit = 1.0.em
        get() = (field * scale)
    val FONTSCALE_LARGE: TextUnit = 1.3.em
        get() = (field * scale)

    // SIZES

    val SIZE_CONTROL_SMALL: Dp = 18.dp
        get() = field * scale

    val SIZE_CONTROL_MEDIUM: Dp = 28.dp
        get() = field * scale

    val SIZE_INSET_SMALL: Dp = 2.dp
        get() = field * scale
    val SIZE_INSET_MEDIUM: Dp = 4.dp
        get() = field * scale
    val SIZE_INSET_LARGE: Dp = 6.dp
        get() = field * scale
    val SIZE_CORNER_RADIUS: Dp = 2.dp
        get() = field * scale
    val SIZE_BORDER_THICKNESS: Dp = 1.dp
        get() = field * scale
    val SIZE_DIVIDER_THICKNESS: Dp = 4.dp
        get() = field * scale
    val SIZE_SCROLL_THUMB: Dp = 8.dp
        get() = field * scale

    val SIZE_BORDER_THICKNESS_MARKED: Dp
        get() = SIZE_BORDER_THICKNESS * 4

    // FONTS


    // BORDERS

}