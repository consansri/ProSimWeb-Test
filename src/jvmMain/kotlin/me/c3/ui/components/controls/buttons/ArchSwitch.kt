package me.c3.ui.components.controls.buttons

import emulator.Link
import me.c3.ui.MainManager
import me.c3.ui.styled.CComboBox
import me.c3.ui.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/**
 * This class represents a combo box used for selecting and switching between different architectures within the application.
 * It retrieves available architectures from the provided MainManager instance through the `Link.entries` property.
 */
class ArchSwitch(mainManager: MainManager) : CComboBox<Link>(mainManager.tm, mainManager.sm, mainManager.icons, Link.entries.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ArchSelectorListener(mainManager))
    }

    /**
     * This inner class listens to the selection events on the ArchSwitch combo box.
     * When a new architecture is selected, it updates the current architecture in the MainManager.
     */
    class ArchSelectorListener(private val mainManager: MainManager): ActionListener{
        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Link ?: return
            mainManager.archManager.curr = selectedMode.load()
        }
    }
}