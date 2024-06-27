package prosim.ui.components.controls

import prosim.ui.components.controls.buttons.*
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent

/**
 * Represents a panel for top controls.
 * @property mainManager The main manager instance.
 * @property showArchSwitch A boolean indicating whether to show the architecture switch button.
 */
class TopControls(showArchSwitch: Boolean) : CPanel(primary = false, borderMode = BorderMode.HORIZONTAL) {

    // List of control buttons
    private val leftButtons: List<JComponent> = if(showArchSwitch) listOf(ArchSwitch()) else listOf()
    private val filler = CLabel("", FontType.BASIC)
    private val rightButtons: List<JComponent> = listOf(ScaleSwitch())

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.VERTICAL

        // Layout
        leftButtons.forEach {
            add(it, gbc)
            gbc.gridx++
        }

        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.BOTH
        add(filler, gbc)
        gbc.gridx++

        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.VERTICAL
        rightButtons.forEach {
            add(it, gbc)
            gbc.gridx++
        }
    }
}