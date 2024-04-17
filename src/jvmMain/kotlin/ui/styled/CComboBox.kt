package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.SwingUtilities

open class CComboBox<T>(themeManager: ThemeManager, scaleManager: ScaleManager, icons: ProSimIcons, array: Array<T>, fontType: FontType) : JComboBox<T>(array) {

    init {
        this.setUI(CComboBoxUI(themeManager, scaleManager, icons, fontType))
    }




}