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
        val inset = UIStates.scale.get().borderScale.insets
         return BorderFactory.createEmptyBorder(inset, inset, inset, inset)
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().globalLaF.bgSecondary
    }


}