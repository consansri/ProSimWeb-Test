package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CIconButtonUI
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.Timer

open class CIconButton(themeManager: ThemeManager, scaleManager: ScaleManager, icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY_NORMAL) : JButton(icon) {

    private var timer: Timer? = null
    private var rotationAngle: Double = 0.0

    var customColor: Color? = null
        set(value) {
            field = value
            (ui as? CIconButtonUI)?.setDefaults(this)
        }

    var rotating = false
        set(value) {
            field = value
            updateAnim()
        }

    var iconBg = themeManager.curr.iconLaF.iconBg
        set(value) {
            field = value
            (ui as? CIconButtonUI)?.setDefaults(this)
        }

    var mode: Mode = mode
        set(value) {
            field = value
            (ui as? CIconButtonUI)?.setDefaults(this)
        }

    var isDeactivated = false
        set(value) {
            field = value
            (ui as? CIconButtonUI)?.setDefaults(this)
        }

    var svgIcon = icon
        set(value) {
            field = value
            (ui as? CIconButtonUI)?.setDefaults(this)
        }

    init {
        this.setUI(CIconButtonUI(themeManager, scaleManager))
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
        SECONDARY_SMALL,
        GRADIENT_NORMAL,
        GRADIENT_SMALL
    }

}
