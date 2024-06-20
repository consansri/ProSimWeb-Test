package me.c3.uilib.styled

import me.c3.uilib.styled.params.FontType
import java.awt.Color
import javax.swing.JTextField

open class CTextField(fontType: FontType) : JTextField() {

    var customBG: Color? = null
        set(value) {
            field = value
            (ui as? CTextFieldUI)?.updateTextColors(this)
        }

    var customFG: Color? = null
        set(value) {
            field = value
            (ui as? CTextFieldUI)?.updateTextColors(this)
        }

    init {
        this.setUI(CTextFieldUI(fontType))
    }

    constructor(text: String, fontType: FontType) : this(fontType) {
        this.text = text
    }
}