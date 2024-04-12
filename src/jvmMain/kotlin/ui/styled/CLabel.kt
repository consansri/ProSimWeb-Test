package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import javax.swing.JLabel
import javax.swing.SwingUtilities

class CLabel(uiManager: UIManager, content: String) : JLabel(content), UIAdapter {

    init {
        this.setupUI(uiManager)
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        foreground = color
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

    override fun setDefaults(uiManager: UIManager) {
        font = uiManager.currTheme().textLaF.getBaseFont().deriveFont(uiManager.currScale().fontScale.textSize)
        border = uiManager.currScale().borderScale.getInsetBorder()
        foreground = uiManager.currTheme().textLaF.base
        repaint()
    }

}