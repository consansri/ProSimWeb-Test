package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JButton

open class CToggleButton(tm: ThemeManager, sm: ScaleManager, initialText: String, toggleSwitchType: CToggleButtonUI.ToggleSwitchType, fontType: FontType) : JButton() {

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
        this.setUI(CToggleButtonUI(tm, sm, toggleSwitchType, fontType))
        text = initialText
    }

}