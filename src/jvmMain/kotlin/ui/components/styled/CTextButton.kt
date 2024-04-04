package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.components.CButtonUI
import me.c3.ui.theme.core.components.CTextButtonUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import java.awt.Cursor
import javax.swing.BorderFactory
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
                val currScale = uiManager.currScale()
                font = it.textStyle.titleFont.deriveFont(currScale.fontScale.titleSize)
                foreground = it.textStyle.base
            }
            uiManager.scaleManager.addScaleChangeEvent {
                val currTheme = uiManager.currTheme()
                font = currTheme.textStyle.titleFont.deriveFont(it.fontScale.titleSize)
                foreground = currTheme.textStyle.base
            }

            // set defaults
            val currTheme = uiManager.currTheme()
            val currScale = uiManager.currScale()
            font = currTheme.textStyle.titleFont.deriveFont(currScale.fontScale.titleSize)
            foreground = currTheme.textStyle.base
        }
    }


}