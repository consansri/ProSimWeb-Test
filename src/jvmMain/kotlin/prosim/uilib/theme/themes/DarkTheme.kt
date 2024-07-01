package prosim.uilib.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.theme.core.Theme
import prosim.uilib.theme.core.style.*
import java.awt.Color

class DarkTheme : Theme {
    override val name: String = "dark"
    override val icon: FlatSVGIcon = UIStates.icon.get().getDarkMode()
    override val dark: Boolean = true

    override val codeLaF: CodeLaF = CodeLaF(
        codeFontPath = "fonts/JetBrainsMono/JetBrainsMono-Regular.ttf",
        markupFontPath =  "fonts/Roboto/Roboto-Regular.ttf",
        pcIdenticator = ">",
        selectionColor = Color(0x777777),
        searchResultColor = Color(0xB68B0F)
    ) {
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
        bgPrimary = Color(0x222222),
        bgSecondary = Color(0x373737),
        bgOverlay = Color(0x222222),
        borderColor = Color(0x777777)
    )

    override val iconLaF: IconLaF = IconLaF(
        iconFgPrimary = Color(0xD5D5D5),
        iconFgSecondary = Color(0xAAAAAA),
        iconBgHover = Color(0x30777777, true),
        iconBgActive = Color(0x50777777, true)
    )
    override val textLaF: TextLaF = TextLaF(
        base = Color(0xD5D5D5),
        baseSecondary = Color(0x777777),
        selected = Color(0x777777),
        fontPath = "fonts/Roboto/Roboto-Regular.ttf",
        titleFontPath = "fonts/Roboto/Roboto-Light.ttf"
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