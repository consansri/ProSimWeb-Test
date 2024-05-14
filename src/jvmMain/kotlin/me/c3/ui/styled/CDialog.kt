package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Component
import javax.swing.JDialog

class CDialog(themeManager: ThemeManager, scaleManager: ScaleManager, parent: Component) : JDialog() {

    init {
        //rootPane = CRootPane(themeManager, scaleManager)
        isAlwaysOnTop = true
        isUndecorated = true
        rootPane.isOpaque = false
        rootPane.background = Color(0,0,0,0)
        contentPane.background = Color(0,0,0,0)
    }

}