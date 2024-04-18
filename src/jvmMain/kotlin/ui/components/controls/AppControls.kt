package me.c3.ui.components.controls

import emulator.kit.optional.Feature
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.MainManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.CToggleButton
import me.c3.ui.styled.CToggleButtonUI
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.Component
import java.awt.ComponentOrientation
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.SwingUtilities

class AppControls(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, BorderMode.WEST) {

    val buttons = listOf(
        ThemeSwitch(mainManager)
    )
    val filler = CPanel(mainManager.themeManager, mainManager.scaleManager)
    val featureButtons = mutableListOf<FeatureSwitch>()

    private val gbc = GridBagConstraints().apply {
        weighty = 0.0
        weightx = 1.0
        insets = Insets(mainManager.scaleManager.curr.controlScale.normalInset, 0, mainManager.scaleManager.curr.controlScale.normalInset, 0)
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

        mainManager.archManager.addFeatureChangeListener {
            updateFeatureButtons()
        }

        mainManager.archManager.addArchChangeListener {
            attachFeatureButtons(mainManager)
        }
        attachFeatureButtons(mainManager)
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

    class FeatureSwitch(private val feature: Feature, mainManager: MainManager) : CToggleButton(mainManager.themeManager, mainManager.scaleManager, feature.name, CToggleButtonUI.ToggleSwitchType.NORMAL, FontType.BASIC) {

        private var switchingFeatures = false

        init {
            mainManager.archManager.addArchChangeListener {
                if (!switchingFeatures) {
                    isActive = feature.isActive()
                }
            }

            this.addActionListener {
                if (!feature.static) {
                    SwingUtilities.invokeLater {
                        switchingFeatures = true
                        if (feature.isActive()) {
                            for (featToUpdate in mainManager.currArch().getAllFeatures()) {
                                if (featToUpdate.enableIDs.contains(feature.id)) {
                                    featToUpdate.deactivate()
                                }
                            }
                        } else {
                            for (id in feature.enableIDs) {
                                mainManager.currArch().getAllFeatures().firstOrNull { it.id == id }?.activate()
                            }
                        }
                        feature.switch()

                        isActive = feature.isActive()
                        mainManager.archManager.triggerFeatureChanged()
                        switchingFeatures = false
                    }
                }
            }

            isActive = feature.isActive()
        }

        fun updateFeatureState() {
            switchingFeatures = true
            isActive = feature.isActive()
            switchingFeatures = false
        }
    }
}