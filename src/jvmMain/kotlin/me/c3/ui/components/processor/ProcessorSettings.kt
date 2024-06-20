package me.c3.ui.components.processor

import emulator.kit.Architecture
import me.c3.ui.Events
import me.c3.ui.States
import me.c3.uilib.styled.CIconButton
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.CPanel
import me.c3.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.lang.ref.WeakReference
import javax.swing.SwingUtilities

class ProcessorSettings(processorView: ProcessorView) : CPanel( primary = false) {

    val increaseRegViews = CIconButton( UIManager.icon.get().increase)
    val decreaseRegViews = CIconButton( UIManager.icon.get().decrease)
    val filler = CLabel( "", FontType.BASIC)
    val pcLabel = CLabel( "", FontType.CODE)

    init {
        attachListeners( processorView)
        attachComponents()

        States.arch.addEvent(WeakReference(this)) {
            updatePC(States.arch.get())
        }
        Events.compile.addListener(WeakReference(this)) {
            updatePC(States.arch.get())
        }
        Events.exe.addListener(WeakReference(this)) {
            updatePC(States.arch.get())
        }
        updatePC(States.arch.get())
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