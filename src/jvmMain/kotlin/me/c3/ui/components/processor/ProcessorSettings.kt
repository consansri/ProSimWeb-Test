package me.c3.ui.components.processor

import emulator.kit.Architecture
import me.c3.ui.manager.ArchManager
import me.c3.ui.manager.EventManager
import me.c3.ui.manager.MainManager
import me.c3.ui.manager.ResManager
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

class ProcessorSettings(processorView: ProcessorView) : CPanel( primary = false) {

    val increaseRegViews = CIconButton( ResManager.icons.increase)
    val decreaseRegViews = CIconButton( ResManager.icons.decrease)
    val filler = CLabel( "", FontType.BASIC)
    val pcLabel = CLabel( "", FontType.CODE)

    init {
        attachListeners( processorView)
        attachComponents()

        ArchManager.addArchChangeListener {
            updatePC(ArchManager.curr)
        }
        EventManager.addCompileListener {
            updatePC(ArchManager.curr)
        }
        EventManager.addExeEventListener {
            updatePC(ArchManager.curr)
        }
        updatePC(ArchManager.curr)
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

    private fun attachListeners(processorView: ProcessorView) {
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
        pcLabel.text = "PC(${arch.regContainer.pc.get().toHex().getRawHexStr()})"
    }


}