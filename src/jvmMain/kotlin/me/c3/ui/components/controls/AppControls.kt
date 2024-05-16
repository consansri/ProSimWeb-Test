package me.c3.ui.components.controls

import emulator.kit.optional.Feature
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.MainManager
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CToggleButton
import me.c3.ui.styled.CToggleButtonUI
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.components.ProSimFrame
import me.c3.ui.components.controls.buttons.FeatureSwitch
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.SwingUtilities

/**
 * This class represents the application control panel within the ProSimFrame window.
 * It contains buttons for various functionalities like theme switching,
 * processor/disassembler toggling, and feature activation.
 */
class AppControls(private val psFrame: ProSimFrame) : CPanel(psFrame.getThemeM(), psFrame.getScaleM(), primary = false, BorderMode.WEST) {
    private var processorShown = false
        set(value) {
            field = value
            psFrame.toggleComponents(processorShown,  consoleAndInfoShown)
        }

    private var consoleAndInfoShown = false
        set(value) {
            field = value
            psFrame.toggleComponents(processorShown,  consoleAndInfoShown)
        }

    private val buttons = listOf(
        ThemeSwitch(psFrame.getThemeM(), psFrame.getScaleM()),
        CIconButton(psFrame.getThemeM(), psFrame.getScaleM(), psFrame.getIcons().processor).apply {
            addActionListener {
                processorShown = !processorShown
                iconBg = if (processorShown) psFrame.getThemeM().curr.iconLaF.iconBgActive else psFrame.getThemeM().curr.iconLaF.iconBg
            }
        },
        CIconButton(psFrame.getThemeM(), psFrame.getScaleM(), psFrame.getIcons().disassembler).apply {
            addActionListener {
                consoleAndInfoShown = !consoleAndInfoShown
                iconBg = if (consoleAndInfoShown) psFrame.getThemeM().curr.iconLaF.iconBgActive else psFrame.getThemeM().curr.iconLaF.iconBg
            }
        },
    )

    val filler = CPanel(psFrame.getThemeM(), psFrame.getScaleM())
    val featureButtons = mutableListOf<FeatureSwitch>()

    private val gbc = GridBagConstraints().apply {
        weighty = 0.0
        weightx = 1.0
        insets = Insets(psFrame.getScaleM().curr.controlScale.normalInset, 0, psFrame.getScaleM().curr.controlScale.normalInset, 0)
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

        psFrame.getArchM().addFeatureChangeListener {
            updateFeatureButtons()
        }

        psFrame.getArchM().addArchChangeListener {
            attachFeatureButtons(psFrame.mManager)
        }
        attachFeatureButtons(psFrame.mManager)
    }

    private fun attachFeatureButtons(mainManager: MainManager) {
        featureButtons.forEach {
            this.remove(it)
            gbc.gridy--
        }
        featureButtons.clear()
        mainManager.currArch().getAllFeatures().filter { !it.invisible && !it.static }.forEach {
            val fswitch = FeatureSwitch(it, mainManager)
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