package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.scale.core.ResizeCondition
import java.awt.Color
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JSplitPane

open class CSplitPane(newOrientation: Int, newContinuousLayout: Boolean, newLeftComponent: Component?, newRightComponent: Component?, primary: Boolean = false) :
    JSplitPane(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent) {

    var primary: Boolean = primary
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CSplitPaneUI())
        this.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                val rComp = rightComponent
                val lComp = leftComponent
                val rResizable = if (rComp is ResizeCondition) rComp.isResizable else true
                val lResizable = if (lComp is ResizeCondition) lComp.isResizable else true
                this@CSplitPane.isOneTouchExpandable = rResizable && lResizable
            }
        })
    }

    constructor(newOrientation: Int, newContinuousLayout: Boolean) : this(newOrientation, newContinuousLayout, null, null)
    constructor(newOrientation: Int) : this(newOrientation, true)

    override fun getDividerSize(): Int {
        return UIStates.scale.get().SIZE_DIVIDER_THICKNESS
    }

    override fun getBackground(): Color {
        return if (primary) {
            UIStates.theme.get().COLOR_BG_0
        } else {
            UIStates.theme.get().COLOR_BG_1
        }
    }

}