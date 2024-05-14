package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import javax.swing.JComboBox

open class CComboBox<T>(themeManager: ThemeManager, scaleManager: ScaleManager, icons: ProSimIcons, array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI(themeManager, scaleManager, icons, fontType))
    }




}