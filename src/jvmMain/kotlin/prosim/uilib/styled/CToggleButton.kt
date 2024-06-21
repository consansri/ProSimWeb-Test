package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JButton

open class CToggleButton( initialText: String, toggleSwitchType: CToggleButtonUI.ToggleSwitchType, fontType: FontType) : JButton() {

    var isDeactivated = false
        set(value) {
            field = value
            (ui as? CToggleButtonUI)?.setDefaults(this)
            repaint()
        }

    var isActive = false
        set(value) {
            field = value
            (ui as? CToggleButtonUI)?.setDefaults(this)
            repaint()
        }

    init {
        this.setUI(CToggleButtonUI( toggleSwitchType, fontType))
        text = initialText
    }

}