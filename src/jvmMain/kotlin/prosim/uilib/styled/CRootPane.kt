package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JRootPane
import javax.swing.border.Border

class CRootPane() : JRootPane() {

    init {
        setUI(CRootPaneUI())
    }

    override fun getBorder(): Border {
        val inset = UIStates.scale.get().SIZE_INSET_MEDIUM
         return BorderFactory.createEmptyBorder(inset, inset, inset, inset)
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().COLOR_BG_1
    }


}