package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CSplitPaneUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Component
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class CSplitPane(themeManager: ThemeManager, scaleManager: ScaleManager, newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component, newRightComponent: Component) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent) {

    init {
        setUI(CSplitPaneUI(themeManager, scaleManager))
    }

}