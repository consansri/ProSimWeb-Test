package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JRootPane

class CRootPane(uiManager: UIManager) : JRootPane(), UIAdapter {

    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager)
        }

        setDefaults(uiManager)
    }

    private fun setDefaults(uiManager: UIManager){
        background = uiManager.currTheme().globalLaF.bgSecondary

        repaint()
    }

}