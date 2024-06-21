package prosim.uilib.styled

import prosim.uilib.UIStates
import javax.swing.JRootPane

class CRootPane() : JRootPane() {
    var cornerRadius = UIStates.scale.get().borderScale.cornerRadius
        set(value) {
            field = value
            (ui as? CRootPaneUI)?.cornerRadius = value
            repaint()
        }

    init {
        setUI(CRootPaneUI())
    }
}