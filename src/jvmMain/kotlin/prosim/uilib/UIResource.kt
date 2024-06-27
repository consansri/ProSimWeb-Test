package prosim.uilib

import prosim.uilib.resource.BenIcons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.scale.scalings.LargeScaling
import prosim.uilib.scale.scalings.LargerScaling
import prosim.uilib.scale.scalings.SmallScaling
import prosim.uilib.scale.scalings.StandardScaling
import prosim.uilib.theme.core.Theme
import prosim.uilib.theme.themes.DarkTheme
import prosim.uilib.theme.themes.LightTheme

object UIResource {
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
        SmallScaling(),
        StandardScaling(),
        LargerScaling(),
        LargeScaling()
    )
}