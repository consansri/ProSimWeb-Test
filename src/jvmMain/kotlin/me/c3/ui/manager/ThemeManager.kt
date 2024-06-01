package me.c3.ui.manager

import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme

/**
 * Manages themes for the application, allowing for theme switching and handling theme change events.
 * @param icons The set of icons to be used by the themes.
 */
object ThemeManager {

    // List of available themes.
    val themes: List<Theme> = listOf(
        LightTheme(),
        DarkTheme()
    )

    // Currently active theme.
    var curr: Theme = themes.first()
        set(value) {
            field = value
            // Trigger theme change events when the current theme is updated.
            triggerThemeChangeEvents()
        }

    // List of theme change event listeners.
    private val themeChangeEvents = mutableListOf<(theme: Theme) -> Unit>()

    // Initialization block to ensure there is at least one theme supplied.
    init {
        assert(themes.isNotEmpty()) {
            throw Exception("No Theme supplied!")
        }
        curr = themes.first()
    }

    /**
     * Adds a listener for theme change events.
     * @param event The event listener to be added.
     */
    fun addThemeChangeListener(event: (theme: Theme) -> Unit) {
        themeChangeEvents.add(event)
    }

    /**
     * Removes a listener for theme change events.
     * @param event The event listener to be removed.
     */
    fun removeThemeChangeEvent(event: (theme: Theme) -> Unit) {
        themeChangeEvents.remove(event)
    }

    /**
     * Triggers all registered theme change events.
     */
    private fun triggerThemeChangeEvents() {
        // Create a copy of the listeners to avoid modification issues during iteration.
        val listenersCopy = ArrayList(themeChangeEvents)
        listenersCopy.forEach {
            it(curr)
        }
    }
}