package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.scale.core.ResizeCondition
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CSplitPaneUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

open class CSplitPane(themeManager: ThemeManager, scaleManager: ScaleManager, newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component, newRightComponent: Component) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent) {

    init {
        this.setUI(CSplitPaneUI(themeManager, scaleManager))
        this.addComponentListener(object : ComponentAdapter(){
            override fun componentResized(e: ComponentEvent?) {
                val rComp = rightComponent
                val lComp = leftComponent
                val rResizable = if(rComp is ResizeCondition) rComp.isResizable else true
                val lResizable = if(lComp is ResizeCondition) lComp.isResizable else true
                this@CSplitPane.isOneTouchExpandable = rResizable && lResizable
            }
        })
    }

}