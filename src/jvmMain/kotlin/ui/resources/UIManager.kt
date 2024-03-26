package me.c3.ui.resources

import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.icons.BenIcons
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme


class UIManager {

    val icons = BenIcons()

    val themes: List<Theme> = listOf(
        LightTheme(icons),
        DarkTheme(icons)
    )

    var currentTheme: Theme = themes.first()
        set(value) {
            field = value
            themeChangeEvents.forEach {
                it(value)
            }
        }

    private val themeChangeEvents = mutableListOf<(theme: Theme) -> Unit>()

    init {
        assert(themes.isNotEmpty()) {
            throw Exception("No Theme supplied!")
        }
        currentTheme = themes.first()
    }

    fun addThemeChangeListener(event: (theme: Theme) -> Unit) {
        themeChangeEvents.add(event)
    }

    fun removeThemeChangeEvent(event: (theme: Theme) -> Unit) {
        themeChangeEvents.remove(event)
    }

}
