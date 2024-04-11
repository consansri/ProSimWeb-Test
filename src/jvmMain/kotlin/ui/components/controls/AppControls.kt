package me.c3.ui.components.controls

import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.UIManager
import me.c3.ui.components.BaseFrame
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextButton
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JFrame

class AppControls(baseFrame: BaseFrame,uiManager: UIManager): CPanel(uiManager, primary = false, BorderMode.WEST) {

    val buttons = listOf(
        ThemeSwitch(uiManager),
        CIconButton(uiManager, uiManager.icons.edit)
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Layout
        buttons.forEach {
            it.alignmentX = Component.CENTER_ALIGNMENT
            add(it)
        }

        buttons[1].addActionListener{
            baseFrame.processorView.regView.registerPaneCount++
        }
    }

}