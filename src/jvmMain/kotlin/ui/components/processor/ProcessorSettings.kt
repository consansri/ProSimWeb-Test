package me.c3.ui.components.processor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

class ProcessorSettings(uiManager: UIManager, processorView: ProcessorView) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = false) {

    val increaseRegViews = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.splitCells)
    val decreaseRegViews = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.combineCells)


    init {
        attachListeners(uiManager, processorView)
        attachComponents()
    }

    private fun attachComponents() {
        SwingUtilities.invokeLater {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(increaseRegViews, gbc)

            gbc.gridx = 1
            add(decreaseRegViews, gbc)
        }
    }

    private fun attachListeners(uiManager: UIManager, processorView: ProcessorView) {
        increaseRegViews.addActionListener {
            if (processorView.regView.registerPaneCount < 4) {
                processorView.regView.registerPaneCount++
            }
        }
        decreaseRegViews.addActionListener {
            if (processorView.regView.registerPaneCount > 1) {
                processorView.regView.registerPaneCount--
            }
        }
    }


}