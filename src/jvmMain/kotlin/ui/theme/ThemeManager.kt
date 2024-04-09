package me.c3.ui.theme

import emulator.kit.nativeLog
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.icons.ProSimIcons
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme
import javax.swing.JFrame
import javax.swing.UIManager

class ThemeManager(icons: ProSimIcons) {

    val themes: List<Theme> = listOf(
        LightTheme(icons),
        DarkTheme(icons)
    )

    var currentTheme: Theme = themes.first()
        set(value) {
            field = value
            //value.install(mainFrame)
            triggerThemeChangeEvents()
        }

    private val themeChangeEvents = mutableListOf<(theme: Theme) -> Unit>()

    init {
        assert(themes.isNotEmpty()) {
            throw Exception("No Theme supplied!")
        }
        currentTheme = themes.first()
    }

    private fun triggerThemeChangeEvents(){
        themeChangeEvents.forEach {
            it(currentTheme)
        }
    }

    fun addThemeChangeListener(event: (theme: Theme) -> Unit) {
        themeChangeEvents.add(event)
    }

    fun removeThemeChangeEvent(event: (theme: Theme) -> Unit) {
        themeChangeEvents.remove(event)
    }

}