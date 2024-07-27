package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.IconSize
import javax.swing.Icon
import javax.swing.JButton

class CSwitch(active: Boolean, iconSize: IconSize = IconSize.PRIMARY_NORMAL, onChange: (Boolean) -> Unit) : JButton() {
    var active: Boolean = active
        set(value) {
            field = value
            repaint()
        }

    var mode: IconSize = iconSize
        set(value) {
            field = value
            repaint()
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
    }

    override fun getIcon(): Icon {
        val onIcon = UIStates.icon.get().switchOn
        val offIcon = UIStates.icon.get().switchOff
        val size = mode.size(UIStates.scale.get())
        return (if (active) onIcon else offIcon).derive(size, size)
    }
}