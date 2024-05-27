package me.c3.ui.styled

import me.c3.ui.scale.core.ResizeCondition
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CSplitPaneUI
import me.c3.ui.theme.ThemeManager
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JSplitPane

open class CSplitPane(tm: ThemeManager, sm: ScaleManager, newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component, newRightComponent: Component) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent) {

    init {
        this.setUI(CSplitPaneUI(tm, sm))
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