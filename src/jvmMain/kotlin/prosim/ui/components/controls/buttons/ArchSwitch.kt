package prosim.ui.components.controls.buttons


import emulator.Link
import prosim.ui.States
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.params.FontType
import java.lang.ref.WeakReference

/**
 * This class represents a combo box used for selecting and switching between different architectures within the application.
 * It retrieves available architectures from the provided MainManager instance through the `Link.entries` property.
 */
class ArchSwitch() : CChooser<Link>(Model(Link.entries, Link.RV32I, "Architecture"), FontType.TITLE, {
    States.arch.set(it.load())
}) {
    init {
        States.arch.addEvent(WeakReference(this)) {
            updateArchFromState()
        }
    }

    private fun updateArchFromState(){
        value = Link.entries.firstOrNull {
            it.classType() == States.arch.get()::class
        } ?: Link.RV32I
    }
}