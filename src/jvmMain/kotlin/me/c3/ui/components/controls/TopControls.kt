package me.c3.ui.components.controls

import me.c3.ui.MainManager
import me.c3.ui.components.controls.buttons.ArchSwitch
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent

/**
 * Represents a panel for top controls.
 * @property mainManager The main manager instance.
 * @property showArchSwitch A boolean indicating whether to show the architecture switch button.
 */
class TopControls(mainManager: MainManager, showArchSwitch: Boolean) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, borderMode = BorderMode.HORIZONTAL) {

    // List of control buttons
    private val buttons: List<JComponent> = if(showArchSwitch) listOf(ArchSwitch(mainManager), me.c3.ui.components.controls.buttons.ScaleSwitch(mainManager)) else listOf(me.c3.ui.components.controls.buttons.ScaleSwitch(mainManager))

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.VERTICAL

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it, gbc)
            gbc.gridx++
        }
    }
}