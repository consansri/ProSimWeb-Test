package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CSplitPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Component
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class CSplitPane(uiManager: UIManager, newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component, newRightComponent: Component) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent), UIAdapter {


    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CSplitPaneUI())

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {
        val cSplitPaneUI = ui as? CSplitPaneUI
        cSplitPaneUI?.dividerColor = uiManager.currTheme().globalLaF.borderColor

        setDividerSize(uiManager.currScale().dividerScale.thickness)
    }

}