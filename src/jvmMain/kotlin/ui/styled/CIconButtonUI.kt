package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import io.nacular.doodle.geometry.circle
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicButtonUI

open class CIconButtonUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager) : BasicButtonUI() {

    var cornerRadius = scaleManager.curr.controlScale.cornerRadius

    companion object {
        val HOVER_COLOR = Color(0x55777777, true)
    }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CIconButton ?: return

        // Set Standard Appearance
        button.isBorderPainted = false
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.isOpaque = false

        scaleManager.addScaleChangeEvent {
            setDefaults(button)
        }

        themeManager.addThemeChangeListener {
            setDefaults(button)
        }

        setDefaults(button)

        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!button.isDeactivated) {
                    button.background = HOVER_COLOR
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                button.background = Color(0, 0, 0, 0) // Reset background to default
            }
        })
    }

    fun setDefaults(cIconButton: CIconButton) {
        cornerRadius = scaleManager.curr.controlScale.cornerRadius
        val inset = getInset(cIconButton)
        cIconButton.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
        updateIcon(cIconButton)
        cIconButton.repaint()
    }

    private fun updateIcon(cIconButton: CIconButton) {
        val iconStyle = themeManager.curr.iconLaF
        SwingUtilities.invokeLater {
            cIconButton.svgIcon?.let {
                when (cIconButton.mode) {
                    CIconButton.Mode.PRIMARY_NORMAL, CIconButton.Mode.PRIMARY_SMALL -> it.colorFilter = FlatSVGIcon.ColorFilter { iconStyle.iconFgPrimary }
                    CIconButton.Mode.SECONDARY_NORMAL, CIconButton.Mode.SECONDARY_SMALL -> it.colorFilter = FlatSVGIcon.ColorFilter { iconStyle.iconFgSecondary }
                    CIconButton.Mode.GRADIENT_NORMAL, CIconButton.Mode.GRADIENT_SMALL -> {}
                }
            }

            cIconButton.background = cIconButton.iconBg
            val iconScale = when (cIconButton.mode) {
                CIconButton.Mode.PRIMARY_NORMAL, CIconButton.Mode.SECONDARY_NORMAL, CIconButton.Mode.GRADIENT_NORMAL -> scaleManager.curr.controlScale.normalSize
                CIconButton.Mode.PRIMARY_SMALL, CIconButton.Mode.SECONDARY_SMALL, CIconButton.Mode.GRADIENT_SMALL -> scaleManager.curr.controlScale.smallSize
            }

            cIconButton.customColor?.let { col ->
                cIconButton.svgIcon?.colorFilter = FlatSVGIcon.ColorFilter {
                    col
                }
            }

            cIconButton.icon = cIconButton.svgIcon?.derive(iconScale, iconScale)
        }
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CIconButton ?: return
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val inset = getInset(button)

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background

        g2.fillRoundRect(inset, inset, width - inset * 2, height - inset * 2, cornerRadius, cornerRadius)

        // Paint button
        super.paint(g, c)
        g2.dispose()
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? CIconButton ?: return super.getPreferredSize(c)
        val preferredSize = super.getPreferredSize(button)
        val inset = getInset(button)
        return Dimension(preferredSize.width + inset * 2, preferredSize.height + inset * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    private fun getInset(cIconButton: CIconButton): Int = when (cIconButton.mode) {
        CIconButton.Mode.PRIMARY_NORMAL, CIconButton.Mode.GRADIENT_NORMAL -> scaleManager.curr.controlScale.normalInset
        CIconButton.Mode.SECONDARY_NORMAL -> scaleManager.curr.controlScale.normalInset
        CIconButton.Mode.PRIMARY_SMALL, CIconButton.Mode.GRADIENT_SMALL -> scaleManager.curr.controlScale.smallInset
        CIconButton.Mode.SECONDARY_SMALL -> scaleManager.curr.controlScale.smallInset
    }
}