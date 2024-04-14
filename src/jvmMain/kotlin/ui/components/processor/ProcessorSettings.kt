package me.c3.ui.components.processor

import emulator.kit.Architecture
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

class ProcessorSettings(uiManager: UIManager, processorView: ProcessorView) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = false) {

    val increaseRegViews = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.splitCells)
    val decreaseRegViews = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.combineCells)
    val filler = CLabel(uiManager.themeManager, uiManager.scaleManager, "")
    val pcLabel = CLabel(uiManager.themeManager, uiManager.scaleManager, "")

    init {
        attachListeners(uiManager, processorView)
        attachComponents()

        uiManager.archManager.addArchChangeListener {
            updatePC(uiManager.currArch())
        }
        uiManager.eventManager.addCompileListener {
            updatePC(uiManager.currArch())
        }
        uiManager.eventManager.addExeEventListener {
            updatePC(uiManager.currArch())
        }
        updatePC(uiManager.currArch())
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

            gbc.gridx = 2
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(filler, gbc)

            gbc.gridx = 3
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(pcLabel, gbc)

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

    private fun updatePC(arch: Architecture) {
        pcLabel.text = "PC(${arch.getRegContainer().pc.get().toHex().getRawHexStr()})"
    }


}