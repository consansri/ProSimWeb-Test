package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.ui.UIManager
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.Timer

open class IconButton(private val uiManager: UIManager, icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY) : JButton(icon) {

    private var timer: Timer? = null
    private var rotationAngle: Double = 0.0

    var customColor: Color? = null
        set(value) {
            field = value
            updateIcon(uiManager)
        }

    var rotating = false
        set(value) {
            field = value
            updateAnim()
        }

    var iconBg = uiManager.currTheme().iconStyle.iconBg
        set(value) {
            field = value
            updateIcon(uiManager)
        }

    var mode: Mode = mode
        set(value) {
            field = value
            updateIcon(uiManager)
        }

    var isDeactivated = false
        set(value) {
            field = value
            updateIcon(uiManager)
        }

    var svgIcon = icon
        set(value) {
            field = value
            updateIcon(uiManager)
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
            updateIcon(uiManager)
        }

        // Add Theme Change Listener
        uiManager.themeManager.addThemeChangeListener {
            updateIcon(uiManager)
        }

        // Add Hover Effect
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                val iconStyle = uiManager.currTheme().iconStyle
                if (!isDeactivated) background = iconStyle.iconBgHover
            }

            override fun mouseExited(e: MouseEvent?) {
                background = iconBg
            }
        })

        updateIcon(uiManager)
    }

    private fun updateIcon(uiManager: UIManager) {
        val iconStyle = uiManager.currTheme().iconStyle
        SwingUtilities.invokeLater {
            svgIcon?.colorFilter = ColorFilter {
                val color =  customColor ?: when (mode) {
                    Mode.PRIMARY -> iconStyle.iconFgPrimary
                    Mode.SECONDARY -> iconStyle.iconFgSecondary
                }

                if (isDeactivated) {
                    Color(color.red, color.green, color.blue, iconStyle.iconDeactivatedAlpha)
                } else {
                    color
                }
            }

            background = iconBg
            val iconScale = uiManager.scaleManager.currentScaling.controlScale.size
            icon = this.svgIcon?.derive(iconScale, iconScale)
        }
    }

    private fun updateAnim() {
        SwingUtilities.invokeLater {
            if (rotating) {
                timer?.stop()
                timer = Timer(50) {
                    rotationAngle += Math.toRadians(5.0)
                    repaint()
                }
                timer?.start()
            } else {
                timer?.stop()
                timer = null
                rotationAngle = 0.0
            }
        }
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as Graphics2D?
        g2d?.let {
            g2d.rotate(rotationAngle, width / 2.0, height / 2.0)
            super.paint(g2d)
            g2d.dispose()
        }
    }

    enum class Mode {
        PRIMARY,
        SECONDARY
    }

}
