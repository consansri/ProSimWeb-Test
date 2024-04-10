package me.c3.ui.components.styled

import me.c3.ui.UIManager
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class CClosableTab(val index: Int, uiManager: UIManager, name: String, primary: Boolean = true, onClose: (Int) -> Unit) : CPanel(uiManager, primary) {

    val closeButton = CIconButton(uiManager, uiManager.icons.cancel, CIconButton.Mode.SECONDARY_SMALL)
    val tabTitle = CLabel(uiManager, name)
    var isClosed = false

    init {
        layout = BorderLayout()

        closeButton.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                if (!isClosed) onClose(index)
                isClosed = true
            }
        })

        this.add(this.tabTitle, BorderLayout.CENTER)
        this.add(closeButton, BorderLayout.EAST)
    }
}