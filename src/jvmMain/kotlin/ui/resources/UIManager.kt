package me.c3.ui.resources

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.icons.BenIcons
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme


class UIManager {

    val icons = BenIcons()

    val themeManager = ThemeManager(icons)
    val scaleManager = ScaleManager()


}
