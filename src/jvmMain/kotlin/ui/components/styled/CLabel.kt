package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.JLabel

class CLabel(uiManager: UIManager, content: String): JLabel(content), UIAdapter {

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
        font = uiManager.currTheme().textStyle.font.deriveFont(uiManager.currScale().fontScale.textSize)
        foreground = uiManager.currTheme().textStyle.base
        repaint()
    }

}