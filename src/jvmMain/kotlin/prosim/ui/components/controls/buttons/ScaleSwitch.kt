package prosim.ui.components.controls.buttons


import prosim.uilib.UIResource
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.params.FontType


class ScaleSwitch() : CChooser<Scaling>(Model<Scaling>(UIResource.scalings, UIStates.scale.get()), FontType.TITLE, {
    UIStates.scale.set(it)
}), StateListener<Scaling> {

    init {
        UIStates.scale.addEvent(this)
    }

    override suspend fun onStateChange(newVal: Scaling) {
        value = newVal
    }
}