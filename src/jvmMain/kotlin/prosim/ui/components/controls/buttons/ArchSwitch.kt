package prosim.ui.components.controls.buttons


import emulator.EmuLink
import emulator.kit.Architecture
import prosim.ui.States
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.params.FontType

/**
 * This class represents a combo box used for selecting and switching between different architectures within the application.
 * It retrieves available architectures from the provided MainManager instance through the `Link.entries` property.
 */
class ArchSwitch : CChooser<EmuLink>(Model(EmuLink.entries, EmuLink.RV32I, "Architecture"), FontType.TITLE, {
    States.arch.set(it.load())
}), StateListener<Architecture> {
    init {
        States.arch.addEvent(this)
    }

    override suspend fun onStateChange(newVal: Architecture) {
        value = EmuLink.entries.firstOrNull {
            it.classType.isInstance(States.arch.get())
        } ?: EmuLink.RV32I
    }
}