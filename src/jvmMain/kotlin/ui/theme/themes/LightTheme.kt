package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.*
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color

class LightTheme(icons: ProSimIcons) : Theme {
    override val name: String = "light"
    override val icon: FlatSVGIcon = icons.lightmode

    override val codeStyle: CodeStyle = CodeStyle(loadFont("fonts/ttf/JetBrainsMono-Regular.ttf")) {
        if (it == null) return@CodeStyle Color(0x222222)
        return@CodeStyle Color(it.lightHexColor)
    }
    override val globalStyle: GlobalStyle = GlobalStyle(Color(0xFFFFFF), Color(0xEEEEEF), Color(0xBBBBBB))
    override val iconStyle: IconStyle = IconStyle(Color(0x222222), Color(0x313131), iconBgHover = Color(0x33777777, true), iconBgActive = Color(0x77777777, true))
    override val textStyle: TextStyle = TextStyle(
        Color(0x222222), Color(0xAAAAAA),
        loadFont("fonts/ttf/JetBrainsMono-Light.ttf"),
        loadFont("fonts/ttf/JetBrainsMono-Bold.ttf")
    )
    override val exeStyle: ExecutionStyle = ExecutionStyle(
        continuous = Color(0x19A744),
        single = Color(0x41A05A),
        multi = Color(0xB68B0F),
        skipSR = Color(0x126EB4),
        returnSR = Color(0xAC5916),
        reassemble = Color(0x9A0000)
    )

}
