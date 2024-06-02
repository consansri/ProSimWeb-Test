package me.c3.ui

import me.c3.ui.resources.icons.BenIcons
import me.c3.ui.scale.core.Scaling
import me.c3.ui.scale.scalings.LargeScaling
import me.c3.ui.scale.scalings.LargerScaling
import me.c3.ui.scale.scalings.SmallScaling
import me.c3.ui.scale.scalings.StandardScaling
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme

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