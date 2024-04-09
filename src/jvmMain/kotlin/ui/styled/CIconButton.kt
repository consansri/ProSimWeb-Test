package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.ui.UIManager
import me.c3.ui.styled.CIconButtonUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.Timer

open class CIconButton(private val uiManager: UIManager, icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY_NORMAL) : JButton(icon), UIAdapter {

    private var timer: Timer? = null
    private var rotationAngle: Double = 0.0


    var customColor: Color? = null
        set(value) {
            field = value
            setupUI(uiManager)
        }

    var rotating = false
        set(value) {
            field = value
            updateAnim()
        }

    var iconBg = uiManager.currTheme().iconLaF.iconBg
        set(value) {
            field = value
            setupUI(uiManager)
        }

    var mode: Mode = mode
        set(value) {
            field = value
            setupUI(uiManager)
        }

    var isDeactivated = false
        set(value) {
            field = value
            setupUI(uiManager)
        }

    var svgIcon = icon
        set(value) {
            field = value
            setupUI(uiManager)
        }

    init {
        setupUI(uiManager)
    }

    private fun updateIcon(uiManager: UIManager) {
        val iconStyle = uiManager.currTheme().iconLaF
        SwingUtilities.invokeLater {
            svgIcon?.colorFilter = ColorFilter {
                val color = customColor ?: when (mode) {
                    Mode.PRIMARY_NORMAL, Mode.PRIMARY_SMALL -> iconStyle.iconFgPrimary
                    Mode.SECONDARY_NORMAL, Mode.SECONDARY_SMALL -> iconStyle.iconFgSecondary
                }

                if (isDeactivated) {
                    Color(color.red, color.green, color.blue, iconStyle.iconDeactivatedAlpha)
                } else {
                    color
                }
            }

            background = iconBg
            val iconScale = when (mode) {
                Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL -> uiManager.scaleManager.currentScaling.controlScale.normalSize
                Mode.PRIMARY_SMALL, Mode.SECONDARY_SMALL -> uiManager.scaleManager.currentScaling.controlScale.smallSize
            }
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
        PRIMARY_NORMAL,
        SECONDARY_NORMAL,
        PRIMARY_SMALL,
        SECONDARY_SMALL
    }

    final override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CIconButtonUI(uiManager, mode))

            // Set Standard Appearance
            isFocusable = false
            isBorderPainted = false

            // Add Scale Change Listener
            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            // Add Theme Change Listener
            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {
        updateIcon(uiManager)

        val buttonUI = this.ui as? CIconButtonUI ?: return
        buttonUI.inset = uiManager.currScale().controlScale.normalInset
        buttonUI.cornerRadius = uiManager.currScale().controlScale.cornerRadius
        repaint()
    }

}
