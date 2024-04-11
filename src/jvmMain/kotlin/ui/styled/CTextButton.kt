package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CTextButtonUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.SwingUtilities

open class CTextButton(private val uiManager: UIManager, text: String) : JButton(text), UIAdapter {
    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var primary = true
        set(value) {
            field = value
            setDefaults(uiManager)
        }

    init {
        this.setupUI(uiManager)
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

    override fun setDefaults(uiManager: UIManager) {
        val currTheme = uiManager.currTheme()
        val currScale = uiManager.currScale()
        border = uiManager.currScale().borderScale.getInsetBorder()
        font = currTheme.textLaF.getTitleFont().deriveFont(currScale.fontScale.titleSize)
        foreground = if (primary) currTheme.textLaF.base else currTheme.textLaF.baseSecondary
        background = Color(0, 0, 0, 0)
        repaint()
    }


}