package prosim.ui.components.controls.buttons


import prosim.uilib.UIResource
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.styled.CComboBox
import prosim.uilib.styled.params.FontType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.ref.WeakReference

/**
 * This class represents a combo box used for selecting and switching between different scaling options within the application.
 * It retrieves available scaling options from the provided MainManager instance.
 */
class ScaleSwitch() : CComboBox<Scaling>(UIResource.scalings.toTypedArray(), FontType.TITLE) {

    init {
        this.addActionListener(ScaleSelectorListener())
        UIStates.scale.addEvent(WeakReference(this)) {
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
            UIStates.scale.set(selectedMode)
        }
    }
}