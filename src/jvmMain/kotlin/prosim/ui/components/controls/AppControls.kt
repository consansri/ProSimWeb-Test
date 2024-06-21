package prosim.ui.components.controls

import prosim.ui.Events
import prosim.ui.States
import prosim.ui.components.ProSimFrame
import prosim.ui.components.controls.buttons.FeatureSwitch
import prosim.ui.components.controls.buttons.Settings
import prosim.ui.components.controls.buttons.ThemeSwitch
import prosim.uilib.UIStates
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.lang.ref.WeakReference

/**
 * This class represents the application control panel within the ProSimFrame window.
 * It contains buttons for various functionalities like theme switching,
 * processor/disassembler toggling, and feature activation.
 */
class AppControls(private val psFrame: ProSimFrame) : CPanel(primary = false, BorderMode.WEST) {
    private var processorShown = false
        set(value) {
            field = value
            psFrame.toggleComponents(processorShown, consoleAndInfoShown)
        }

    private var consoleAndInfoShown = false
        set(value) {
            field = value
            psFrame.toggleComponents(processorShown, consoleAndInfoShown)
        }

    private val buttons = listOf(
        ThemeSwitch(),
        CIconButton(UIStates.icon.get().processor).apply {
            addActionListener {
                processorShown = !processorShown
                iconBg = if (processorShown) UIStates.theme.get().iconLaF.iconBgActive else UIStates.theme.get().iconLaF.iconBg
            }
        },
        CIconButton(UIStates.icon.get().disassembler).apply {
            addActionListener {
                consoleAndInfoShown = !consoleAndInfoShown
                iconBg = if (consoleAndInfoShown) UIStates.theme.get().iconLaF.iconBgActive else UIStates.theme.get().iconLaF.iconBg
            }
        },
        Settings()
    )

    val filler = CPanel()
    val featureButtons = mutableListOf<FeatureSwitch>()

    private val gbc = GridBagConstraints().apply {
        weighty = 0.0
        weightx = 1.0
        insets = Insets(UIStates.scale.get().controlScale.normalInset, 0, UIStates.scale.get().controlScale.normalInset, 0)
        gridx = 0
        gridy = 0
        fill = GridBagConstraints.HORIZONTAL
    }

    init {
        layout = GridBagLayout()

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it, gbc)
            gbc.gridy++
        }

        gbc.fill = GridBagConstraints.BOTH
        gbc.weighty = 1.0
        add(filler, gbc)
        gbc.gridy++
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weighty = 0.0

        Events.archFeatureChange.addListener(WeakReference(this)) {
            updateFeatureButtons()
        }

        States.arch.addEvent(WeakReference(this)) {
            attachFeatureButtons()
        }
        attachFeatureButtons()
    }

    private fun attachFeatureButtons() {
        featureButtons.forEach {
            this.remove(it)
            gbc.gridy--
        }
        featureButtons.clear()
        States.arch.get().features.filter { !it.invisible && !it.static }.forEach {
            val fswitch = FeatureSwitch(it)
            fswitch.alignmentX = Component.CENTER_ALIGNMENT
            featureButtons.add(fswitch)
            add(fswitch, gbc)
            gbc.gridy++
        }
    }

    private fun updateFeatureButtons() {
        featureButtons.forEach {
            it.updateFeatureState()
        }
    }
}