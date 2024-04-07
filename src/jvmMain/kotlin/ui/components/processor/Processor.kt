package me.c3.ui.components.processor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import javax.swing.BoxLayout

class Processor(uiManager: UIManager): CPanel(uiManager, primary = false) {

    val exeControl = ExecutionControls(uiManager)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(exeControl)
        



    }

}