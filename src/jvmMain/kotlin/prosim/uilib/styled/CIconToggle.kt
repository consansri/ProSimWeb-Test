package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.border.Border

class CIconToggle(val svgIcon: FlatSVGIcon, active: Boolean = false, val mode: CIconButton.Mode = CIconButton.Mode.PRIMARY_NORMAL, onChange: (Boolean) -> Unit) : JButton() {

    var active: Boolean = active
        set(value) {
            field = value
            repaint()
        }

    val activeBG: Color
        get() = UIStates.theme.get().iconLaF.iconBgActive


    val inactiveBG: Color
        get() = UIStates.theme.get().iconLaF.iconBg

    init {
        // Set Standard Appearance
        isBorderPainted = false
        isContentAreaFilled = false
        isFocusPainted = false
        isFocusable = false
        isOpaque = false

        addActionListener {
            this@CIconToggle.active = !this@CIconToggle.active
            onChange(this@CIconToggle.active)
        }
    }

    override fun getBackground(): Color {
        return if (active) activeBG else inactiveBG
    }

    override fun getIcon(): Icon {
        mode.applyFilter(svgIcon, UIStates.theme.get())
        val size = mode.size(UIStates.scale.get())
        return svgIcon.derive(size, size)
    }

    override fun getBorder(): Border {
        return UIStates.scale.get().borderScale.getInsetBorder()
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return super.paint(g)

        g2d.paint = background
        g2d.fillRoundRect(0, 0, width, height, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)

        super.paint(g2d)
        g2d.dispose()
    }

}