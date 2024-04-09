package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CTextButtonUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import javax.swing.JButton
import javax.swing.SwingUtilities

class CTextButton(uiManager: UIManager, text: String) : JButton(text), UIAdapter {
    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            setUI(CTextButtonUI())

            // apply listeners
            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }
            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            // set defaults
            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager){
        val currTheme = uiManager.currTheme()
        val currScale = uiManager.currScale()
        font = currTheme.textLaF.getTitleFont().deriveFont(currScale.fontScale.titleSize)
        foreground = currTheme.textLaF.base
        background = Color(0,0,0,0)
        repaint()
    }


}