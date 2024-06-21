package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.lang.ref.WeakReference
import javax.swing.JButton

class CIconToggle(val svgIcon: FlatSVGIcon, active: Boolean, val mode: CIconButton.Mode = CIconButton.Mode.PRIMARY_NORMAL, onChange: (Boolean) -> Unit) : JButton() {

    var active = active
        set(value) {
            field = value
            updateIcon()
        }

    var activeBG: Color = UIStates.theme.get().iconLaF.iconBgActive
        set(value) {
            field = value
            updateIcon()
        }

    var inactiveBG: Color = UIStates.theme.get().iconLaF.iconBg
        set(value) {
            field = value
            updateIcon()
        }

    init {
        // Set Standard Appearance
        isBorderPainted = false
        isContentAreaFilled = false
        isFocusPainted = false
        isFocusable = false
        isOpaque = false

        UIStates.theme.addEvent(WeakReference(this)) {
            setDefaults()
        }

        UIStates.scale.addEvent(WeakReference(this)) {
            setDefaults()
        }

        addActionListener {
            this@CIconToggle.active = !this@CIconToggle.active
            onChange(this@CIconToggle.active)
        }
        setDefaults()
    }

    fun updateIcon() {
        background = if (active) activeBG else inactiveBG
        val size = mode.size(UIStates.scale.get())
        icon = svgIcon.derive(size, size)
    }

    fun setDefaults() {
        activeBG = UIStates.theme.get().iconLaF.iconBgActive
        inactiveBG = UIStates.theme.get().iconLaF.iconBg
        mode.applyFilter(svgIcon, UIStates.theme.get())
        border = UIStates.scale.get().borderScale.getInsetBorder()
        updateIcon()
        revalidate()
        repaint()
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return super.paint(g)

        g2d.paint = background
        g2d.fillRoundRect(0, 0, width, height, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)

        super.paint(g2d)
        g2d.dispose()
    }

}