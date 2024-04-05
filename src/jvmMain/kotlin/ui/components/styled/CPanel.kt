package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.components.CPanelUI
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

open class CPanel(uiManager: UIManager, private val primary: Boolean) : JPanel(), UIAdapter {

    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            setUI(CPanelUI())

            uiManager.themeManager.addThemeChangeListener {
                background = if (primary) it.globalStyle.bgPrimary else it.globalStyle.bgSecondary
            }

            val currTheme = uiManager.currTheme()
            background = if (primary) currTheme.globalStyle.bgPrimary else currTheme.globalStyle.bgSecondary
            border = BorderFactory.createEmptyBorder()
        }
    }

}