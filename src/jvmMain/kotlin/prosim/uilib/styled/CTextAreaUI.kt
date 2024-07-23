package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        c.isOpaque = false
        c.background = Color(0,0,0,0)
    }
}