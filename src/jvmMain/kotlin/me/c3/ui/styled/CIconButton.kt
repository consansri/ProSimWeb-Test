package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.States
import me.c3.ui.scale.core.Scaling
import me.c3.ui.theme.core.Theme
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.Timer

open class CIconButton(icon: FlatSVGIcon? = null, mode: Mode = Mode.PRIMARY_NORMAL, var hasHoverEffect: Boolean = true) : JButton(icon) {

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

    var iconBg = States.theme.get().iconLaF.iconBg
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
        this.setUI(CIconButtonUI())
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
        GRADIENT_SMALL;

        fun applyFilter(icon: FlatSVGIcon, theme: Theme) {
            when (this) {
                PRIMARY_NORMAL, PRIMARY_SMALL -> icon.colorFilter = FlatSVGIcon.ColorFilter { theme.iconLaF.iconFgPrimary }
                SECONDARY_NORMAL, SECONDARY_SMALL -> icon.colorFilter = FlatSVGIcon.ColorFilter { theme.iconLaF.iconFgSecondary }
                GRADIENT_NORMAL, GRADIENT_SMALL -> {}
            }
        }

        fun size(scale: Scaling): Int = when (this) {
            PRIMARY_NORMAL, SECONDARY_NORMAL, GRADIENT_NORMAL -> scale.controlScale.normalSize
            PRIMARY_SMALL, SECONDARY_SMALL, GRADIENT_SMALL -> scale.controlScale.smallSize
        }


        fun getInset(): Int = when (this) {
            PRIMARY_NORMAL, GRADIENT_NORMAL -> States.scale.get().controlScale.normalInset
            SECONDARY_NORMAL -> States.scale.get().controlScale.normalInset
            PRIMARY_SMALL, GRADIENT_SMALL -> States.scale.get().controlScale.smallInset
            SECONDARY_SMALL -> States.scale.get().controlScale.smallInset
        }

    }

}
