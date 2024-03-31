package me.c3.ui.components.controls

import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.UIManager
import me.c3.ui.components.layout.ColouredPanel
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

class AppControls(uiManager: UIManager, mainFrame: JFrame): JPanel() {

    val buttons = listOf(
        ThemeSwitch(uiManager, mainFrame)
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Listeners
        uiManager.scaleManager.addScaleChangeEvent {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        }

        uiManager.themeManager.addThemeChangeListener {
            background = it.globalStyle.bgSecondary
        }

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }

        // Set Defaults
        val insets = uiManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = uiManager.currTheme().globalStyle.bgPrimary
    }

}