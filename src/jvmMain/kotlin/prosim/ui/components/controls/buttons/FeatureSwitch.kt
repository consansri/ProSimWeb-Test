package prosim.ui.components.controls.buttons

import emulator.kit.Architecture
import emulator.kit.optional.Feature
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CToggleButton
import prosim.uilib.styled.CToggleButtonUI
import prosim.uilib.styled.params.FontType
import javax.swing.SwingUtilities

/**
 * This class represents a button used for enabling/disabling functionalities within the application based on a Feature object.
 */
class FeatureSwitch(private val feature: Feature) : CToggleButton(feature.name, CToggleButtonUI.ToggleSwitchType.NORMAL, FontType.BASIC), StateListener<Architecture> {

    private var switchingFeatures = false

    init {
        States.arch.addEvent(this)

        this.addActionListener {
            if (!feature.static) {
                SwingUtilities.invokeLater {
                    switchingFeatures = true
                    if (feature.isActive()) {
                        for (featToUpdate in States.arch.get().features) {
                            if (featToUpdate.enableIDs.contains(feature.id)) {
                                featToUpdate.deactivate()
                            }
                        }
                    } else {
                        for (id in feature.enableIDs) {
                            States.arch.get().features.firstOrNull { it.id == id }?.activate()
                        }
                    }
                    feature.switch()

                    isActive = feature.isActive()
                    Events.archFeatureChange.triggerEvent(States.arch.get())
                    switchingFeatures = false
                }
            }
        }

        isActive = feature.isActive()
    }

    override suspend fun onStateChange(newVal: Architecture) {
        if (!switchingFeatures) {
            isActive = feature.isActive()
        }
    }

    fun updateFeatureState() {
        switchingFeatures = true
        isActive = feature.isActive()
        switchingFeatures = false
    }
}