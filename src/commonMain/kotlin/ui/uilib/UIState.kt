package ui.uilib

import androidx.compose.runtime.staticCompositionLocalOf
import ui.uilib.resource.BenIcons
import ui.uilib.resource.Icons
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.Theme

object UIState {

    val Theme = staticCompositionLocalOf<Theme> { DarkTheme }
    val Icon = staticCompositionLocalOf<Icons> { BenIcons }


}