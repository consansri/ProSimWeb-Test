package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.States
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.*
import java.awt.Color

class LightTheme : Theme {
    override val name: String = "light"
    override val icon: FlatSVGIcon = States.icon.get().getLightMode()
    override val dark: Boolean = false

    override val codeLaF: CodeLaF = CodeLaF(
        font = loadFont("fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"),
        pcIdenticator = ">",
        selectionColor = Color(0x4d90fe),
        searchResultColor = Color(0xE2B124)
    ) {
        if (it == null) return@CodeLaF Color(0x333333)
        return@CodeLaF Color(it.lightHexColor)
    }

    override val dataLaF: DataLaF = DataLaF {
        Color(it.light)
    }

    override val globalLaF: GlobalLaF = GlobalLaF(
        bgPrimary = Color(0xFFFFFF),
        bgSecondary = Color(0xF0F2F5),
        bgOverlay = Color(0xFFFFFF),
        borderColor = Color(0xCDCDCD)
    )

    override val iconLaF: IconLaF = IconLaF(
        iconFgPrimary = Color(0x333333),
        iconFgSecondary = Color(0x666666),
        iconBgActive = Color(0x50777777, true),
        iconBg = Color(0x00EFEFEF, true),
        iconBgHover = Color(0x20777777, true)
    )

    override val textLaF: TextLaF = TextLaF(
        base = Color(0x454545),
        baseSecondary = Color(0x999999),
        selected = Color(0xE0E2E5),
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
