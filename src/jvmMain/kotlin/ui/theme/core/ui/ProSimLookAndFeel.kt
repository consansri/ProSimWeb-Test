package me.c3.ui.theme.core.ui

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.ui.FlatButtonUI
import com.formdev.flatlaf.ui.FlatInternalFrameUI
import com.formdev.flatlaf.ui.FlatLabelUI
import com.formdev.flatlaf.ui.FlatListCellBorder
import com.formdev.flatlaf.ui.FlatListUI
import com.formdev.flatlaf.ui.FlatPanelUI
import com.formdev.flatlaf.ui.FlatRootPaneUI
import com.formdev.flatlaf.ui.FlatScrollPaneUI
import com.formdev.flatlaf.ui.FlatTabbedPaneUI
import com.formdev.flatlaf.ui.FlatTextPaneUI
import com.formdev.flatlaf.ui.FlatTreeUI
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme
import java.awt.Color
import javax.swing.LookAndFeel
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.RootPaneUI
import javax.swing.plaf.basic.BasicLookAndFeel

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

        defaults["RootPaneUI"] = FlatRootPaneUI::class.java.name
        defaults["ButtonUI"] = FlatButtonUI::class.java.name
        defaults["TextPaneUI"] = FlatTextPaneUI::class.java.name
        defaults["TreeUI"] = FlatTreeUI::class.java.name
        defaults["ScrollPaneUI"] = FlatScrollPaneUI::class.java.name
        defaults["TabbedPaneUI"] = FlatTabbedPaneUI::class.java.name
        defaults["PanelUI"] = FlatPanelUI::class.java.name
        defaults["ListUI"] = FlatListUI::class.java.name
        defaults["LabelUI"] = FlatLabelUI::class.java.name

        applyThemeToUIDefaults(defaults, theme)

        return defaults
    }

    private fun applyThemeToUIDefaults(defaults: UIDefaults, theme: Theme) {

    }
}