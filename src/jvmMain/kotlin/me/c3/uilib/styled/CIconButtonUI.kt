package me.c3.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.uilib.UIStates
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicButtonUI

open class CIconButtonUI() : BasicButtonUI() {

    var cornerRadius = UIStates.scale.get().controlScale.cornerRadius
    var hoverColor = UIStates.theme.get().iconLaF.iconBgHover

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CIconButton ?: return

        // Set Standard Appearance
        button.isBorderPainted = false
        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = true
        button.isOpaque = false

        UIStates.scale.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }

        UIStates.theme.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }

        setDefaults(button)

        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (button.hasHoverEffect) {
                    if (!button.isDeactivated) {
                        button.background = hoverColor
                    }
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                if (button.hasHoverEffect) {
                    button.background = button.iconBg
                }
            }
        })
    }

    fun setDefaults(btn: CIconButton) {
        SwingUtilities.invokeLater {
            cornerRadius = UIStates.scale.get().controlScale.cornerRadius
            hoverColor = UIStates.theme.get().iconLaF.iconBgHover
            val inset = btn.mode.getInset()
            btn.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
            updateIcon(btn)
            btn.repaint()
        }
    }

    private fun updateIcon(cIconButton: CIconButton) {
        val theme = UIStates.theme.get()

        cIconButton.svgIcon?.let {
            cIconButton.mode.applyFilter(it, theme)
            if (cIconButton.isDeactivated) {
                it.colorFilter = FlatSVGIcon.ColorFilter {
                    theme.iconLaF.iconFgSecondary
                }
            }
        }

        cIconButton.background = cIconButton.iconBg
        val iconScale = cIconButton.mode.size(UIStates.scale.get())

        cIconButton.customColor?.let { col ->
            cIconButton.svgIcon?.colorFilter = FlatSVGIcon.ColorFilter {
                col
            }
        }

        cIconButton.icon = cIconButton.svgIcon?.derive(iconScale, iconScale)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CIconButton ?: return
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val inset = button.mode.getInset()

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
        val inset = button.mode.getInset()
        return Dimension(preferredSize.width + inset * 2, preferredSize.height + inset * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }


}