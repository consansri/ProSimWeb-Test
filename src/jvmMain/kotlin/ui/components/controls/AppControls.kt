package me.c3.ui.components.controls

import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JFrame

class AppControls(uiManager: UIManager): CPanel(uiManager, primary = false, BorderMode.WEST) {

    val buttons = listOf(
        ThemeSwitch(uiManager)
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }
    }

}