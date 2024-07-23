package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JComboBox

open class CComboBox<T>(array: Array<T>, val fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI( fontType))
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().textLaF.base
    }

}