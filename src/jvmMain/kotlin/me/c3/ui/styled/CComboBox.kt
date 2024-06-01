package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import javax.swing.JComboBox

open class CComboBox<T>(icons: ProSimIcons, array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI( icons, fontType))
    }




}