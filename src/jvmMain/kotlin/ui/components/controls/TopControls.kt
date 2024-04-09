package me.c3.ui.components.controls

import me.c3.ui.UIManager
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.components.styled.CPanel
import java.awt.Component
import javax.swing.BoxLayout

class TopControls(uiManager: UIManager) : CPanel(uiManager, primary = false) {

    val buttons = listOf(
        ArchSwitch(uiManager)
    )

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }
    }

}