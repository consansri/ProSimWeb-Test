package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicButtonUI

class CToggleButtonUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val toggleSwitchType: ToggleSwitchType, private val fontType: FontType) : BasicButtonUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CToggleButton ?: return
        button.horizontalAlignment = SwingConstants.CENTER

        themeManager.addThemeChangeListener {
            setDefaults( button)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults( button)
        }

        setDefaults( button)
    }

    fun setDefaults(button: CToggleButton) {
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.font = fontType.getFont(themeManager, scaleManager)
        button.border = when (toggleSwitchType) {
            ToggleSwitchType.SMALL -> scaleManager.curr.controlScale.getSmallInsetBorder()
            ToggleSwitchType.NORMAL -> scaleManager.curr.controlScale.getNormalInsetBorder()
        }
        button.size = when (toggleSwitchType) {
            ToggleSwitchType.SMALL -> scaleManager.curr.controlScale.getSmallSize()
            ToggleSwitchType.NORMAL -> scaleManager.curr.controlScale.getNormalSize()
        }
        button.background = if (button.isActive) themeManager.curr.iconLaF.iconBgActive else themeManager.curr.iconLaF.iconBg
        button.foreground = if (button.isDeactivated) themeManager.curr.textLaF.baseSecondary else themeManager.curr.textLaF.base
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CToggleButton ?: return
        val g2 = g as? Graphics2D ?: return

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(0, 0, width , height, scaleManager.curr.controlScale.cornerRadius, scaleManager.curr.controlScale.cornerRadius)

        // Paint button
        super.paint(g2, c)
        g2.dispose()
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? CToggleButton ?: return super.getPreferredSize(c)
        val fm = button.getFontMetrics(button.font)
        val preferredSize = Dimension(fm.stringWidth(button.text) + c.insets.left + c.insets.right, fm.height + c.insets.top + c.insets.bottom)
        val minimumSize = getMinimumSize(c)
        return Dimension(maxOf(preferredSize.width, minimumSize.width), maxOf(preferredSize.height, minimumSize.height))
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        val button = c as? CToggleButton ?: return super.getPreferredSize(c)
        val preferredSize = when(toggleSwitchType){
            ToggleSwitchType.SMALL -> scaleManager.curr.controlScale.getSmallSize()
            ToggleSwitchType.NORMAL -> scaleManager.curr.controlScale.getNormalSize()
        }
        return Dimension(preferredSize.width + c.insets.left + c.insets.right, preferredSize.height + c.insets.top + c.insets.bottom)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    enum class ToggleSwitchType {
        SMALL,
        NORMAL
    }
    
    private fun getInset() =  when (toggleSwitchType) {
        ToggleSwitchType.SMALL -> scaleManager.curr.controlScale.smallInset
        ToggleSwitchType.NORMAL -> scaleManager.curr.controlScale.normalInset
    }

}