package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CPanelUI
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

open class CPanel(uiManager: UIManager, private val primary: Boolean = false, private val topRoot: Boolean = false, private val bottomRoot: Boolean = false) : JPanel(), UIAdapter {

    init {
        this.setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CPanelUI())

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    private fun setDefaults(uiManager: UIManager) {
        background = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary
        border = uiManager.currScale().borderScale.getInsetBorder()
        repaint()
    }

}