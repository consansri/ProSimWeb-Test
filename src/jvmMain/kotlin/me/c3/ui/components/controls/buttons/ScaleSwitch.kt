package me.c3.ui.components.controls.buttons


import me.c3.ui.Res
import me.c3.ui.States
import me.c3.ui.scale.core.Scaling
import me.c3.ui.styled.CComboBox
import me.c3.ui.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.ref.WeakReference

/**
 * This class represents a combo box used for selecting and switching between different scaling options within the application.
 * It retrieves available scaling options from the provided MainManager instance.
 */
class ScaleSwitch() : CComboBox<Scaling>(Res.scalings.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ScaleSelectorListener())
        States.scale.addEvent(WeakReference(this)) {
            selectedItem = it
        }
    }

    /**
     * This inner class listens to the selection events on the ScaleSwitch combo box.
     * When a new scaling option is selected, it updates the current scaling mode in the MainManager.
     */
    class ScaleSelectorListener() : ActionListener {

        override fun actionPerformed(e: ActionEvent?) {
            val comboBox = e?.source as? CComboBox<*>
            val selectedMode = comboBox?.selectedItem as? Scaling ?: return
            States.scale.set(selectedMode)
        }
    }
}