package me.c3.ui.styled

import me.c3.ui.styled.params.FontType
import me.c3.ui.resources.icons.ProSimIcons
import javax.swing.JComboBox

open class CComboBox<T>(array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI( fontType))
    }
}