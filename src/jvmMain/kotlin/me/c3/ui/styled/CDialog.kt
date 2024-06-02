package me.c3.ui.styled

import java.awt.Color
import java.awt.Component
import javax.swing.JDialog

class CDialog(parent: Component) : JDialog() {

    init {
        //rootPane = CRootPane(tm, sm)
        isAlwaysOnTop = true
        isUndecorated = true
        rootPane.isOpaque = false
        rootPane.background = Color(0,0,0,0)
        contentPane.background = Color(0,0,0,0)
    }

}