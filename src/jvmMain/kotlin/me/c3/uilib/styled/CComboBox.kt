package me.c3.uilib.styled

import me.c3.uilib.styled.params.FontType
import javax.swing.JComboBox

open class CComboBox<T>(array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI( fontType))
    }
}