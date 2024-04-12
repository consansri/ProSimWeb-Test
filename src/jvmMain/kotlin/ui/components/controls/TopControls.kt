package me.c3.ui.components.controls

import me.c3.ui.UIManager
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.ColouredPanel
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JComponent

class TopControls(uiManager: UIManager) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = false, borderMode = BorderMode.SOUTH) {

    val buttons: List<JComponent> = listOf()

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }
    }
}