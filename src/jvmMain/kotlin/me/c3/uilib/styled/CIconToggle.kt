package me.c3.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.uilib.UIManager
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

    var activeBG: Color = UIManager.theme.get().iconLaF.iconBgActive
        set(value) {
            field = value
            updateIcon()
        }

    var inactiveBG: Color = UIManager.theme.get().iconLaF.iconBg
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

        UIManager.theme.addEvent(WeakReference(this)) {
            setDefaults()
        }

        UIManager.scale.addEvent(WeakReference(this)) {
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
        val size = mode.size(UIManager.scale.get())
        icon = svgIcon.derive(size, size)
    }

    fun setDefaults() {
        activeBG = UIManager.theme.get().iconLaF.iconBgActive
        inactiveBG = UIManager.theme.get().iconLaF.iconBg
        mode.applyFilter(svgIcon, UIManager.theme.get())
        border = UIManager.scale.get().borderScale.getInsetBorder()
        updateIcon()
        revalidate()
        repaint()
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return super.paint(g)

        g2d.paint = background
        g2d.fillRoundRect(0, 0, width, height, UIManager.scale.get().borderScale.cornerRadius, UIManager.scale.get().borderScale.cornerRadius)

        super.paint(g2d)
        g2d.dispose()
    }

}