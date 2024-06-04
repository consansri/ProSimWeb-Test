package me.c3.ui.styled

import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import javax.swing.JSpinner
import javax.swing.SpinnerModel

class CSpinner(model: SpinnerModel, fontType: FontType = FontType.BASIC,val borderMode: BorderMode = BorderMode.HORIZONTAL) : JSpinner(model) {

    var fontType: FontType = fontType
        set(value) {
            field = value
            (ui as? CSpinnerUI)?.setDefaults(this)
        }

    init {
        setUI(CSpinnerUI())
    }

}