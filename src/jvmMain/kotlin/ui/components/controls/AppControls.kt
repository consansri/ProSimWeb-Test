package me.c3.ui.components.controls

import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.UIManager
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

class AppControls(uiManager: UIManager, mainFrame: JFrame): JPanel() {

    val buttons = listOf(
        ThemeSwitch(uiManager, mainFrame),
        ThemeSwitch(uiManager, mainFrame),
        ThemeSwitch(uiManager, mainFrame)
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        buttons.first().isDeactivated = true
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }
    }
}