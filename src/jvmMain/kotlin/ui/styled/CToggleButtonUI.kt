package me.c3.ui.styled

import me.c3.ui.UIManager
import java.awt.*
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class CToggleButtonUI(private val uiManager: UIManager, private val toggleSwitchType: ToggleSwitchType) : BasicButtonUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CToggleButton ?: return

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager, button)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager, button)
        }

        setDefaults(uiManager, button)
    }

    private fun setDefaults(uiManager: UIManager, button: CToggleButton) {
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.border = when (toggleSwitchType) {
            ToggleSwitchType.SMALL -> uiManager.currScale().controlScale.getSmallInsetBorder()
            ToggleSwitchType.NORMAL -> uiManager.currScale().controlScale.getNormalInsetBorder()
        }

        button.background = if (button.isActive) uiManager.currTheme().iconLaF.iconBgActive else uiManager.currTheme().iconLaF.iconBg
        button.foreground = if (button.isDeactivated) uiManager.currTheme().textLaF.baseSecondary else uiManager.currTheme().textLaF.base
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CToggleButton ?: return
        val g2 = g as? Graphics2D ?: return

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(button.inset, button.inset, width - button.inset * 2, height - button.inset * 2, uiManager.currScale().controlScale.cornerRadius, uiManager.currScale().controlScale.cornerRadius)

        // Paint button
        super.paint(g, c)
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? CToggleButton ?: return super.getPreferredSize(c)
        val preferredSize = when(toggleSwitchType){
            ToggleSwitchType.SMALL -> uiManager.currScale().controlScale.getSmallSize()
            ToggleSwitchType.NORMAL -> uiManager.currScale().controlScale.getNormalSize()
        }
        return Dimension(preferredSize.width + button.inset * 2, preferredSize.height + button.inset * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    enum class ToggleSwitchType {
        SMALL,
        NORMAL
    }

}