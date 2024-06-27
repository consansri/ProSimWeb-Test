package prosim.ui.components.controls.buttons


import prosim.uilib.UIResource
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.params.FontType
import java.lang.ref.WeakReference


class ScaleSwitch() : CChooser<Scaling>(Model<Scaling>(UIResource.scalings, UIStates.scale.get()), FontType.TITLE, {
    UIStates.scale.set(it)
}) {

    init {
        UIStates.scale.addEvent(WeakReference(this)) {
            value = it
        }
    }
}