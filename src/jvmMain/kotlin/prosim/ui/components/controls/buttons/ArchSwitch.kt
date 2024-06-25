package prosim.ui.components.controls.buttons

import emulator.Link
import prosim.ui.States


import prosim.uilib.styled.CComboBox
import prosim.uilib.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.ref.WeakReference

/**
 * This class represents a combo box used for selecting and switching between different architectures within the application.
 * It retrieves available architectures from the provided MainManager instance through the `Link.entries` property.
 */
class ArchSwitch() : CComboBox<Link>(Link.entries.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ArchSelectorListener())

        States.arch.addEvent(WeakReference(this)) { arch ->
            selectedItem = Link.entries.firstOrNull {
                it.classType() == arch::class
            }
        }
    }

    /**
     * This inner class listens to the selection events on the ArchSwitch combo box.
     * When a new architecture is selected, it updates the current architecture in the MainManager.
     */
    class ArchSelectorListener(): ActionListener{
        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Link ?: return
            States.arch.set(selectedMode.load())
        }
    }
}