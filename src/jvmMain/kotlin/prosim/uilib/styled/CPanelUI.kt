package prosim.uilib.styled

import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class CPanelUI() : ComponentUI() {

    override fun installUI(c: JComponent?) {
        c as? CPanel ?: return super.installUI(c)

        c.isOpaque = false
        c.background = Color(0, 0, 0, 0)
    }

}