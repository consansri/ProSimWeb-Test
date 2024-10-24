package prosim.ui.components.processor

import emulator.kit.Architecture
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

class ProcessorSettings(processorView: ProcessorView) : CPanel(primary = false), StateListener<Architecture> {

    val increaseRegViews = CIconButton(UIStates.icon.get().increase)
    val decreaseRegViews = CIconButton(UIStates.icon.get().decrease)
    val filler = CLabel("", FontType.BASIC)
    val pcLabel = CLabel("", FontType.CODE)

    val compileListener = Events.compile.createAndAddListener {
        updatePC(States.arch.get())
    }

    val exeListener = Events.exe.createAndAddListener {
        updatePC(States.arch.get())
    }

    init {
        attachListeners(processorView)
        attachComponents()

        States.arch.addEvent(this)

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
        pcLabel.text = "PC(${arch.regContainer.pc.get().toHex().toRawString()})"
    }

    override suspend fun onStateChange(newVal: Architecture) {
        updatePC(newVal)
    }


}