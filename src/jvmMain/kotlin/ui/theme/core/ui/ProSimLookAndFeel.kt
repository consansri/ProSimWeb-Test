package me.c3.ui.theme.core.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme
import javax.swing.LookAndFeel
import javax.swing.UIDefaults

class ProSimLookAndFeel(private val theme: Theme) : LookAndFeel() {
    override fun getName(): String = theme.name

    override fun getID(): String = "ProSimLookAndFeel"

    override fun getDescription(): String = "Theme based Look and Feel"

    override fun isNativeLookAndFeel(): Boolean = false

    override fun isSupportedLookAndFeel(): Boolean = true

    override fun getDefaults(): UIDefaults {
        val defaults = when (theme) {
            is LightTheme -> FlatLightLaf().defaults
            is DarkTheme -> FlatDarkLaf().defaults
            else -> {
                FlatLightLaf().defaults
            }
        }

        applyThemeToUIDefaults(defaults, theme)

        return defaults
    }

    private fun applyThemeToUIDefaults(defaults: UIDefaults, theme: Theme) {

    }
}