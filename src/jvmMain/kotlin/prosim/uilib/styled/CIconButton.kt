package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.Timer

open class CIconButton(icon: FlatSVGIcon, mode: Mode = Mode.PRIMARY_NORMAL, hoverEffect: Boolean = true) : JComponent() {

    private var timer: Timer? = null
    var rotationAngle: Double = 0.0
        private set

    var customColor: Color? = null
        set(value) {
            field = value
            repaint()
        }

    var rotating = false
        set(value) {
            field = value
            updateAnim()
        }

    var iconBg = UIStates.theme.get().iconLaF.iconBg
        set(value) {
            field = value
            repaint()
        }

    var isHovered: Boolean = false
        set(value) {
            field = value
            repaint()
        }

    var mode: Mode = mode
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var svgIcon = icon
        set(value) {
            field = value
            revalidate()
            repaint()
        }
        get() {
            val size = mode.size(UIStates.scale.get())
            val icon = field
            val customColor = customColor

            if (customColor != null) {
                icon.colorFilter = FlatSVGIcon.ColorFilter {
                    customColor
                }
            } else {
                mode.applyFilter(icon, UIStates.theme.get())
            }

            if (isDeactivated) {
                icon.colorFilter = FlatSVGIcon.ColorFilter {
                    UIStates.theme.get().iconLaF.iconFgInactive
                }
            }

            return icon.derive(size, size)
        }

    init {
        this.setUI(CIconButtonUI())
        if (hoverEffect) {
            installHoverEffect()
        }
    }

    fun addActionListener(event: () -> Unit) {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                event()
            }
        })
    }

    private fun installHoverEffect() {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                isHovered = true
            }

            override fun mouseExited(e: MouseEvent?) {
                isHovered = false
            }
        })
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
            PRIMARY_NORMAL, GRADIENT_NORMAL -> UIStates.scale.get().controlScale.normalInset
            SECONDARY_NORMAL -> UIStates.scale.get().controlScale.normalInset
            PRIMARY_SMALL, GRADIENT_SMALL -> UIStates.scale.get().controlScale.smallInset
            SECONDARY_SMALL -> UIStates.scale.get().controlScale.smallInset
        }

    }
}
