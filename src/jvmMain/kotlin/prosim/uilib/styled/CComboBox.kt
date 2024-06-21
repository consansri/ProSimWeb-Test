package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JComboBox

open class CComboBox<T>(array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI( fontType))
    }
}