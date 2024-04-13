package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.spacing.core.Scaling
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.Theme
import javax.swing.JFileChooser

class CFileChooser(themeManager: ThemeManager, scaleManager: ScaleManager) : JFileChooser() {

    init {
        //this.setUI(CFileChooserUI(themeManager, scaleManager, this))
    }
}