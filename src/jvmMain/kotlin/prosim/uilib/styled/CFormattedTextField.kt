package prosim.uilib.styled

import prosim.uilib.CFormattedTextFieldUI
import prosim.uilib.styled.params.FontType
import java.awt.Color
import javax.swing.JFormattedTextField

class CFormattedTextField(fontType: FontType): JFormattedTextField() {
    var customBG: Color? = null
        set(value) {
            field = value
            (ui as? CFormattedTextFieldUI)?.updateTextColors(this)
        }

    var customFG: Color? = null
        set(value) {
            field = value
            (ui as? CFormattedTextFieldUI)?.updateTextColors(this)
        }
    init {
        setUI(CFormattedTextFieldUI(fontType))
    }

}