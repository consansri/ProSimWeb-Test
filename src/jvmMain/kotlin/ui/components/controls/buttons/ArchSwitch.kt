package me.c3.ui.components.controls.buttons

import emulator.Link
import me.c3.ui.MainManager
import me.c3.ui.styled.CComboBox
import me.c3.ui.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ArchSwitch(mainManager: MainManager) : CComboBox<Link>(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, Link.entries.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ArchSelectorListener(mainManager))
    }

    class ArchSelectorListener(private val mainManager: MainManager): ActionListener{
        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Link ?: return
            mainManager.archManager.curr = selectedMode.arch
        }
    }

}