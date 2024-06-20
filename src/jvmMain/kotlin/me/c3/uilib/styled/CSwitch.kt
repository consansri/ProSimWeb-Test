package me.c3.uilib.styled

import me.c3.uilib.UIStates
import javax.swing.JButton

class CSwitch(active: Boolean, mode: CIconButton.Mode = CIconButton.Mode.PRIMARY_NORMAL, onChange: (Boolean) -> Unit) : JButton() {
    var active: Boolean = active
        set(value) {
            field = value
            setDefaults()
        }

    var mode: CIconButton.Mode = mode
        set(value) {
            field = value
            setDefaults()
        }

    init {
        // Set Standard Appearance
        isBorderPainted = false
        isContentAreaFilled = false
        isFocusable = false
        isOpaque = false
        addActionListener {
            this@CSwitch.active = !this@CSwitch.active
            onChange(this@CSwitch.active)
        }
        setDefaults()
    }

    fun updateIcon() {
        val onIcon = UIStates.icon.get().switchOn
        val offIcon = UIStates.icon.get().switchOff
        val size = mode.size(UIStates.scale.get())
        icon = (if (active) onIcon else offIcon).derive(size, size)
    }

    fun setDefaults() {
        updateIcon()
        revalidate()
        repaint()
    }
}