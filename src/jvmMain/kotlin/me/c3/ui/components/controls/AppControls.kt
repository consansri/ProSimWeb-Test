package me.c3.ui.components.controls

import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.components.ProSimFrame
import me.c3.ui.components.controls.buttons.FeatureSwitch
import me.c3.ui.manager.*
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets

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
        CIconButton(ResManager.icons.processor).apply {
            addActionListener {
                processorShown = !processorShown
                iconBg = if (processorShown) ThemeManager.curr.iconLaF.iconBgActive else ThemeManager.curr.iconLaF.iconBg
            }
        },
        CIconButton(ResManager.icons.disassembler).apply {
            addActionListener {
                consoleAndInfoShown = !consoleAndInfoShown
                iconBg = if (consoleAndInfoShown) ThemeManager.curr.iconLaF.iconBgActive else ThemeManager.curr.iconLaF.iconBg
            }
        },
    )

    val filler = CPanel()
    val featureButtons = mutableListOf<FeatureSwitch>()

    private val gbc = GridBagConstraints().apply {
        weighty = 0.0
        weightx = 1.0
        insets = Insets(ScaleManager.curr.controlScale.normalInset, 0, ScaleManager.curr.controlScale.normalInset, 0)
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

        ArchManager.addFeatureChangeListener {
            updateFeatureButtons()
        }

        ArchManager.addArchChangeListener {
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
        ArchManager.curr.features.filter { !it.invisible && !it.static }.forEach {
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