package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.CodeStyle
import me.c3.ui.theme.core.style.GlobalStyle
import me.c3.ui.theme.core.style.IconStyle
import me.c3.ui.theme.core.style.TextStyle
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color

class DarkTheme(icons: ProSimIcons) : Theme {
    override val name: String = "dark"
    override val icon: FlatSVGIcon = icons.darkmode

    override val codeStyle: CodeStyle = CodeStyle(loadFont("fonts/ttf/JetBrainsMono-Regular.ttf")) {
        if(it == null) return@CodeStyle Color(0xEEEEEE)
        return@CodeStyle Color(it.getDarkElseLight())
    }
    override val globalStyle: GlobalStyle = GlobalStyle(Color(0x222222), Color(0x313131), Color(0x363636))

    override val iconStyle: IconStyle = IconStyle(Color(0xEEEEEE), Color(0xAAAAAA), iconBgHover = Color(0x33777777, true), iconBgActive = Color(0x77777777, true))
    override val textStyle: TextStyle = TextStyle(Color(0xEEEEEE), Color(0x777777), loadFont("fonts/ttf/JetBrainsMono-Light.ttf"))

}