package me.c3.ui.components.controls

import me.c3.ui.MainManager
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JComponent

class TopControls(mainManager: MainManager, showArchSwitch: Boolean) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, borderMode = BorderMode.SOUTH) {

    val buttons: List<JComponent> = if(showArchSwitch) listOf(ArchSwitch(mainManager)) else listOf()

    init {
        layout = BorderLayout()

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it, BorderLayout.CENTER)
        }
    }
}