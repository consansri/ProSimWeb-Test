package me.c3.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.uilib.UIStates
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.Timer

open class CIconButton(icon: FlatSVGIcon, mode: Mode = Mode.PRIMARY_NORMAL, val hasHoverEffect: Boolean = true) : JComponent() {

    private var timer: Timer? = null
    private var rotationAngle: Double = 0.0

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

    var iconBgHover = UIStates.theme.get().iconLaF.iconBgHover
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

    init {
        this.setUI(CIconButtonUI())
        if(hasHoverEffect){
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

    fun getIcon(): Icon {
        val size = mode.size(UIStates.scale.get())
        val icon = svgIcon
        val customColor = customColor

        if (customColor != null) {
            icon.colorFilter = FlatSVGIcon.ColorFilter {
                customColor
            }
        } else {
            mode.applyFilter(icon, UIStates.theme.get())
        }

        if (isDeactivated) {
            icon.colorFilter = FlatSVGIcon.ColorFilter{
                UIStates.theme.get().iconLaF.iconFgInactive
            }
        }

        return icon.derive(size, size)
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as Graphics2D?
        g2d?.let {
            g2d.rotate(rotationAngle, width / 2.0, height / 2.0)
            super.paint(g2d)
            g2d.dispose()
        }
    }

    private fun installHoverEffect(){
        this.addMouseListener(object : MouseAdapter(){
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
