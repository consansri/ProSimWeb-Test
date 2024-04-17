package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.*
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color

class LightTheme(icons: ProSimIcons) : Theme {
    override val name: String = "light"
    override val icon: FlatSVGIcon = icons.lightmode
    override val dark: Boolean = false

    override val codeLaF: CodeLaF = CodeLaF(
        font = loadFont("fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"),
        pcIdenticator = ">",
        selectionColor = Color(0x3399FF)
    ) {
        if (it == null) return@CodeLaF Color(0x222222)
        return@CodeLaF Color(it.lightHexColor)
    }

    override val dataLaF: DataLaF = DataLaF {
        Color(it.light)
    }

    override val globalLaF: GlobalLaF = GlobalLaF(
        bgPrimary = Color(0xFFFFFF),
        bgSecondary = Color(0xE0E0FF),
        bgOverlay = Color(0xFFFFFF),
        borderColor = Color(0xBBBBBB)
    )

    override val iconLaF: IconLaF = IconLaF(
        iconFgPrimary = Color(0x222222),
        iconFgSecondary = Color(0x313131),
        iconBg = Color(0x00777777, true),
        iconBgHover = Color(0x77777777, true)
    )

    override val textLaF: TextLaF = TextLaF(
        base = Color(0x222222),
        baseSecondary = Color(0xAAAAAA),
        selelected = Color(0xBBBBBB),
        font = loadFont("fonts/Roboto/Roboto-Regular.ttf"),
        titleFont = loadFont("fonts/Roboto/Roboto-Light.ttf")
    )

    override val exeStyle: ExeLaF = ExeLaF(
        continuous = Color(0x19A744),
        single = Color(0x41A05A),
        multi = Color(0xB68B0F),
        skipSR = Color(0x126EB4),
        returnSR = Color(0xAC5916),
        reassemble = Color(0x9A0000)
    )

}
