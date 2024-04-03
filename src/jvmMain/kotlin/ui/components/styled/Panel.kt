package me.c3.ui.components.styled

import me.c3.ui.UIManager
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

open class Panel(uiManager: UIManager, primary: Boolean) : JPanel() {

    init {
        SwingUtilities.invokeLater {
            uiManager.themeManager.addThemeChangeListener {
                background = if (primary) it.globalStyle.bgPrimary else it.globalStyle.bgSecondary
            }

            val currTheme = uiManager.currTheme()
            background = if (primary) currTheme.globalStyle.bgPrimary else currTheme.globalStyle.bgSecondary
            border = BorderFactory.createEmptyBorder()
        }
    }

}