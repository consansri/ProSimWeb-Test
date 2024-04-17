package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.*
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color

class DarkTheme(icons: ProSimIcons) : Theme {
    override val name: String = "dark"
    override val icon: FlatSVGIcon = icons.darkmode
    override val dark: Boolean = true

    override val codeLaF: CodeLaF = CodeLaF(loadFont("fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"), ">", Color(0x777777)) {
        if (it == null) return@CodeLaF Color(0xEEEEEE)
        return@CodeLaF Color(it.getDarkElseLight())
    }

    override val dataLaF: DataLaF = DataLaF {
        if (it.dark != null) {
            Color(it.dark)
        } else {
            Color(it.light)
        }
    }

    override val globalLaF: GlobalLaF = GlobalLaF(
        Color(0x222222),
        Color(0x313131),
        Color(0x222222),
        Color(0x777777)
    )

    override val iconLaF: IconLaF = IconLaF(
        Color(0xEEEEEE),
        Color(0xAAAAAA),
        iconBgHover = Color(0x33777777, true),
        iconBgActive = Color(0x77777777, true)
    )
    override val textLaF: TextLaF = TextLaF(
        Color(0xEEEEEE),
        Color(0x777777),
        Color(0x777777),
        loadFont("fonts/Roboto/Roboto-Regular.ttf"),
        loadFont("fonts/Roboto/Roboto-Light.ttf")
    )

    override val exeStyle: ExeLaF = ExeLaF(
        continuous = Color(0x58CC79),
        single = Color(0x98D8AA),
        multi = Color(0xE2B124),
        skipSR = Color(0x549FD8),
        returnSR = Color(0xEE9955),
        reassemble = Color(0xEE2222)
    )
}