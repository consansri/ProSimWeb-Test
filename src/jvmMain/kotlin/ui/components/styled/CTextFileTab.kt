package me.c3.ui.components.styled

import me.c3.ui.UIManager
import java.awt.BorderLayout
import java.io.File
import javax.swing.JLabel

class CTextFileTab(uiManager: UIManager, tabbedPane: CTabbedPane, val filePath: String, primary: Boolean = true) : CPanel(uiManager, primary) {

    val closeButton = CIconButton(uiManager, uiManager.icons.cancel, CIconButton.Mode.SECONDARY_SMALL)
    val tabTitle = CLabel(uiManager, File(filePath).path)

    init {
        layout = BorderLayout()

        closeButton.addActionListener {
            val index = tabbedPane.indexOfTabComponent(this@CTextFileTab)
            if (index != -1) {
                tabbedPane.removeTabAt(index)
            }
        }

        this.add(this.tabTitle, BorderLayout.CENTER)
        this.add(closeButton, BorderLayout.EAST)
    }

}