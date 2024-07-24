package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import javax.swing.JPanel
import javax.swing.border.AbstractBorder

open class CRawPanel( border: AbstractBorder? = null): JPanel() {

    init {
        this.setUI(CRawPanelUI( border))
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().COLOR_BG_0
    }
}