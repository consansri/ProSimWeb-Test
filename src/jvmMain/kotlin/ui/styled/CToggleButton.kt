package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JButton

open class CToggleButton(themeManager: ThemeManager, scaleManager: ScaleManager, initialText: String, toggleSwitchType: CToggleButtonUI.ToggleSwitchType, fontType: FontType) : JButton() {

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
        setUI(CToggleButtonUI(themeManager, scaleManager, toggleSwitchType, fontType))
        text = initialText
    }

}