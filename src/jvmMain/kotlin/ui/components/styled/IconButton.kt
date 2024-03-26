package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.ui.resources.UIManager
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton

open class IconButton(uiManager: UIManager, icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY) : JButton(icon) {

    var iconStyle = uiManager.currentTheme.iconStyle
        set(value) {
            field = value
            updateIcon()
        }

    var mode: Mode = mode
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
        uiManager.addThemeChangeListener {
            iconStyle = it.iconStyle
        }

        size = Dimension(28, 28)
        isFocusable = false

        background = iconStyle.iconBgSecondary

        isBorderPainted = false

        this.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                background = iconStyle.iconBgPrimary
            }

            override fun mouseExited(e: MouseEvent?) {
                background = iconStyle.iconBgSecondary
            }
        })
    }

    private fun updateIcon() {
        svgIcon?.colorFilter = ColorFilter {
            when (mode) {
                Mode.PRIMARY -> iconStyle.iconFgPrimary
                Mode.SECONDARY -> iconStyle.iconFgSecondary
            }
        }

        background = iconStyle.iconBgSecondary

        icon = svgIcon
    }

    enum class Mode {
        PRIMARY,
        SECONDARY
    }

}
