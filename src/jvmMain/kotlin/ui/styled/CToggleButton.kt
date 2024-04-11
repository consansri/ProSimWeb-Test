package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTextButton
import javax.swing.JButton

open class CToggleButton(private val uiManager: UIManager, initialText: String, private val toggleSwitchType: CToggleButtonUI.ToggleSwitchType) : JButton() {

    var isDeactivated = false
        set(value) {
            field = value
            setUI(CToggleButtonUI(uiManager, toggleSwitchType))
        }

    var isActive = false
        set(value) {
            field = value
            setUI(CToggleButtonUI(uiManager, toggleSwitchType))
        }

    var inset = when (toggleSwitchType) {
        CToggleButtonUI.ToggleSwitchType.SMALL -> uiManager.currScale().controlScale.smallInset
        CToggleButtonUI.ToggleSwitchType.NORMAL -> uiManager.currScale().controlScale.normalInset
    }
        set(value) {
            field = value
            repaint()
        }

    init {
        text = initialText
        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults()
        }
        setDefaults()
    }

    private fun setDefaults() {
        size = when (toggleSwitchType) {
            CToggleButtonUI.ToggleSwitchType.SMALL -> uiManager.currScale().controlScale.getSmallSize()
            CToggleButtonUI.ToggleSwitchType.NORMAL -> uiManager.currScale().controlScale.getNormalSize()
        }
    }

}