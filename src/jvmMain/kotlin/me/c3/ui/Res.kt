package me.c3.ui

import me.c3.uilib.resource.BenIcons
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.scale.scalings.LargeScaling
import me.c3.uilib.scale.scalings.LargerScaling
import me.c3.uilib.scale.scalings.SmallScaling
import me.c3.uilib.scale.scalings.StandardScaling
import me.c3.uilib.theme.core.Theme
import me.c3.uilib.theme.themes.DarkTheme
import me.c3.uilib.theme.themes.LightTheme

object Res {

    val icons = listOf(
        BenIcons()
    )

    // List of available themes.
    val themes: List<Theme> = listOf(
        LightTheme(),
        DarkTheme()
    )

    // List of available scaling options.
    val scalings: List<Scaling> = listOf(
        StandardScaling(),
        SmallScaling(),
        LargerScaling(),
        LargeScaling()
    )
}