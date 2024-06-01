package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
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