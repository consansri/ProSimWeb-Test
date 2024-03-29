package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.ui.UIManager
import java.awt.Color
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton

open class IconButton(uiManager: UIManager, icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY) : JButton(icon) {
    var iconStyle = uiManager.themeManager.currentTheme.iconStyle
        set(value) {
            field = value
            updateIcon()
        }

    var mode: Mode = mode
        set(value) {
            field = value
            updateIcon()
        }

    var isDeactivated = false
        set(value) {
            field = value
            updateIcon()
        }

    var svgIcon = icon
        set(value) {
            field = value
            updateIcon()
        }

    init {
        // Set Standard Appearance
        isFocusable = false
        isBorderPainted = false

        // Add Scale Change Listener
        val iconScale = uiManager.scaleManager.currentScaling.controlScale.size
        size = Dimension(iconScale, iconScale)
        uiManager.scaleManager.addScaleChangeEvent {
            size = Dimension(it.controlScale.size, it.controlScale.size)
        }

        // Add Theme Change Listener
        iconStyle = uiManager.themeManager.currentTheme.iconStyle
        uiManager.themeManager.addThemeChangeListener {
            iconStyle = it.iconStyle
        }

        // Add Hover Effect
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!isDeactivated) background = iconStyle.iconBgHover
            }

            override fun mouseExited(e: MouseEvent?) {
                background = iconStyle.iconBg
            }
        })

        updateIcon()
    }

    private fun updateIcon() {
        svgIcon?.colorFilter = ColorFilter {
            val color = when (mode) {
                Mode.PRIMARY -> iconStyle.iconFgPrimary
                Mode.SECONDARY -> iconStyle.iconFgSecondary
            }
            if (isDeactivated) {
                Color(color.red, color.green, color.blue, iconStyle.iconDeactivatedAlpha)
            } else {
                color
            }
        }

        background = iconStyle.iconBg

        icon = svgIcon
    }

    enum class Mode {
        PRIMARY,
        SECONDARY
    }

}
