package me.c3.ui.components.controls.buttons

import emulator.Link
import me.c3.ui.UIManager
import me.c3.ui.styled.CComboBox
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ArchSwitch(uiManager: UIManager) : CComboBox<Link>(uiManager.themeManager, uiManager.scaleManager, uiManager.icons, Link.entries.toTypedArray()) {

    init {
        this.addActionListener(ArchSelectorListener(uiManager))
    }

    class ArchSelectorListener(private val uiManager: UIManager): ActionListener{
        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Link ?: return
            uiManager.archManager.curr = selectedMode.arch
        }
    }


}