package me.c3.ui.components.styled

import me.c3.ui.UIManager
import java.awt.BorderLayout
import java.awt.event.ActionEvent

class CClosableTab(uiManager: UIManager, name: String, primary: Boolean = true, onClose: (ActionEvent) -> Unit) : CPanel(uiManager, primary) {

    val closeButton = CIconButton(uiManager, uiManager.icons.cancel, CIconButton.Mode.SECONDARY_SMALL)
    val tabTitle = CLabel(uiManager, name)

    init {
        layout = BorderLayout()

        closeButton.addActionListener {
            onClose(it)
        }

        this.add(this.tabTitle, BorderLayout.CENTER)
        this.add(closeButton, BorderLayout.EAST)
    }

}