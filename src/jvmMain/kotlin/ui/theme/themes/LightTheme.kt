package me.c3.ui.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.core.style.CodeStyle
import me.c3.ui.theme.core.style.GlobalStyle
import me.c3.ui.theme.core.style.IconStyle
import me.c3.ui.theme.core.style.TextStyle
import me.c3.ui.theme.icons.ProSimIcons
import me.c3.ui.spacing.IconSpacing
import java.awt.Color

class LightTheme(icons: ProSimIcons) : Theme {
    override val name: String = "light"
    override val icon: FlatSVGIcon = icons.lightmode

    override val codeStyle: CodeStyle = CodeStyle(loadFont("fonts/ttf/JetBrainsMono-Regular.ttf").deriveFont(12f))
    override val globalStyle: GlobalStyle = GlobalStyle(Color(0xEEEEEE), Color(0xAAAAAA), Color(0x777777))
    override val iconStyle: IconStyle = IconStyle(Color(0x222222), Color(0x313131))
    override val textStyle: TextStyle = TextStyle(Color(0x222222), loadFont("fonts/ttf/JetBrainsMono-Light.ttf").deriveFont(12f))

}
