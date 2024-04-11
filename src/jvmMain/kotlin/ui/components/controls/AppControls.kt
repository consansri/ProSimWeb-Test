package me.c3.ui.components.controls

import emulator.kit.optional.Feature
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.UIManager
import me.c3.ui.components.BaseFrame
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextButton
import me.c3.ui.styled.CToggleButton
import me.c3.ui.styled.CToggleButtonUI
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JFrame

class AppControls(baseFrame: BaseFrame, uiManager: UIManager) : CPanel(uiManager, primary = false, BorderMode.WEST) {

    val buttons = listOf(
        ThemeSwitch(uiManager)
    )
    val featureButtons = mutableListOf<FeatureSwitch>()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }

        uiManager.archManager.addArchChangeListener {
            attachFeatureButtons(uiManager)
        }
        attachFeatureButtons(uiManager)
    }

    private fun attachFeatureButtons(uiManager: UIManager) {
        featureButtons.forEach {
            this.remove(it)
        }
        featureButtons.clear()
        uiManager.currArch().getAllFeatures().filter { !it.invisible }.forEach {
            val fswitch = FeatureSwitch(it, uiManager)
            fswitch.alignmentX = Component.CENTER_ALIGNMENT
            featureButtons.add(fswitch)
            add(fswitch)
        }
    }

    class FeatureSwitch(private val feature: Feature, uiManager: UIManager) : CToggleButton(uiManager, feature.name, CToggleButtonUI.ToggleSwitchType.NORMAL) {

        private var switchingFeatures = false

        init {
            uiManager.archManager.addArchChangeListener {
                if (!switchingFeatures) {
                    isActive = feature.isActive()
                }
            }

            this.addActionListener {
                if (!feature.static) {
                    switchingFeatures = true
                    feature.switch()
                    isActive = feature.isActive()
                    uiManager.archManager.triggerFeatureChanged()
                    switchingFeatures = false
                }
            }

            isActive = feature.isActive()
        }
    }
}