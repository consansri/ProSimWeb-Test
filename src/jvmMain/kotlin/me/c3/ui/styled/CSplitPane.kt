package me.c3.ui.styled

import me.c3.ui.scale.core.ResizeCondition
import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JSplitPane

open class CSplitPane( newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component, newRightComponent: Component) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent) {

    init {
        this.setUI(CSplitPaneUI())
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