package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JButton

open class CTextButton( text: String, fontType: FontType) : JButton(text) {
    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var primary = true
        set(value) {
            field = value
            (ui as? CTextButtonUI)?.setDefaults(this)
        }

    init {
        this.setUI(CTextButtonUI( fontType))
    }
}