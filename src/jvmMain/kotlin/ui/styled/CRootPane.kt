package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JRootPane
import javax.swing.SwingUtilities

class CRootPane(uiManager: UIManager) : JRootPane(), UIAdapter {

    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager){
        background = uiManager.currTheme().globalLaF.bgSecondary

        repaint()
    }

}