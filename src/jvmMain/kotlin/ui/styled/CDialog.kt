package me.c3.ui.styled

import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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