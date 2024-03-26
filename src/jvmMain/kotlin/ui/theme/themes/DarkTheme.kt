package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.spacing.IconSpacing
import me.c3.ui.theme.core.style.CodeStyle
import me.c3.ui.theme.core.style.GlobalStyle
import me.c3.ui.theme.core.style.IconStyle
import me.c3.ui.theme.core.style.TextStyle
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color

class DarkTheme(icons: ProSimIcons) : Theme {
    override val name: String = "dark"
    override val icon: FlatSVGIcon = icons.darkmode

    override val codeStyle: CodeStyle = CodeStyle(loadFont("fonts/ttf/JetBrainsMono-Regular.ttf").deriveFont(10f))
    override val globalStyle: GlobalStyle = GlobalStyle(Color(0x222222), Color(0x313131), Color(0x777777))

    override val iconStyle: IconStyle = IconStyle(Color(0xEEEEEE), Color(0xAAAAAA))
    override val textStyle: TextStyle = TextStyle(Color(0xEEEEEE), loadFont("fonts/ttf/JetBrainsMono-Light.ttf").deriveFont(10f))

}