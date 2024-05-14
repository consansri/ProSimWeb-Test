package me.c3.ui.components.processor

import emulator.kit.Architecture
import me.c3.ui.MainManager
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

class ProcessorSettings(mainManager: MainManager, processorView: ProcessorView) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false) {

    val increaseRegViews = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.increase)
    val decreaseRegViews = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.decrease)
    val filler = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.BASIC)
    val pcLabel = CLabel(mainManager.themeManager, mainManager.scaleManager, "", FontType.CODE)

    init {
        attachListeners(mainManager, processorView)
        attachComponents()

        mainManager.archManager.addArchChangeListener {
            updatePC(mainManager.currArch())
        }
        mainManager.eventManager.addCompileListener {
            updatePC(mainManager.currArch())
        }
        mainManager.eventManager.addExeEventListener {
            updatePC(mainManager.currArch())
        }
        updatePC(mainManager.currArch())
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

    private fun attachListeners(mainManager: MainManager, processorView: ProcessorView) {
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