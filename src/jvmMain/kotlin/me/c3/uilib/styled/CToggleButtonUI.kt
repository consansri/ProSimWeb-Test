package me.c3.uilib.styled

import me.c3.uilib.UIStates
import me.c3.uilib.styled.params.FontType
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicButtonUI

class CToggleButtonUI(private val toggleSwitchType: ToggleSwitchType, private val fontType: FontType) : BasicButtonUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CToggleButton ?: return
        button.horizontalAlignment = SwingConstants.CENTER

        UIStates.theme.addEvent(WeakReference(button)) { _ ->
            setDefaults( button)
        }

        UIStates.scale.addEvent(WeakReference(button)) { _ ->
            setDefaults( button)
        }

        setDefaults( button)
    }

    fun setDefaults(button: CToggleButton) {
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.font = fontType.getFont()
        button.border = when (toggleSwitchType) {
            ToggleSwitchType.SMALL -> UIStates.scale.get().controlScale.getSmallInsetBorder()
            ToggleSwitchType.NORMAL -> UIStates.scale.get().controlScale.getNormalInsetBorder()
        }
        button.size = when (toggleSwitchType) {
            ToggleSwitchType.SMALL -> UIStates.scale.get().controlScale.getSmallSize()
            ToggleSwitchType.NORMAL -> UIStates.scale.get().controlScale.getNormalSize()
        }
        button.background = if (button.isActive) UIStates.theme.get().iconLaF.iconBgActive else UIStates.theme.get().iconLaF.iconBg
        button.foreground = if (button.isDeactivated) UIStates.theme.get().textLaF.baseSecondary else UIStates.theme.get().textLaF.base
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CToggleButton ?: return
        val g2 = g as? Graphics2D ?: return

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(0, 0, width , height, UIStates.scale.get().controlScale.cornerRadius, UIStates.scale.get().controlScale.cornerRadius)

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
            ToggleSwitchType.SMALL -> UIStates.scale.get().controlScale.getSmallSize()
            ToggleSwitchType.NORMAL -> UIStates.scale.get().controlScale.getNormalSize()
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
        ToggleSwitchType.SMALL -> UIStates.scale.get().controlScale.smallInset
        ToggleSwitchType.NORMAL -> UIStates.scale.get().controlScale.normalInset
    }

}