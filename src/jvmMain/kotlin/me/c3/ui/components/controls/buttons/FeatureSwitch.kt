package me.c3.ui.components.controls.buttons

import emulator.kit.optional.Feature
import me.c3.ui.MainManager
import me.c3.ui.styled.CToggleButton
import me.c3.ui.styled.CToggleButtonUI
import me.c3.ui.styled.params.FontType
import javax.swing.SwingUtilities

/**
 * This class represents a button used for enabling/disabling functionalities within the application based on a Feature object.
 */
class FeatureSwitch(private val feature: Feature, mainManager: MainManager) : CToggleButton(mainManager.tm, mainManager.sm, feature.name, CToggleButtonUI.ToggleSwitchType.NORMAL, FontType.BASIC) {

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
                        for (featToUpdate in mainManager.currArch().features) {
                            if (featToUpdate.enableIDs.contains(feature.id)) {
                                featToUpdate.deactivate()
                            }
                        }
                    } else {
                        for (id in feature.enableIDs) {
                            mainManager.currArch().features.firstOrNull { it.id == id }?.activate()
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