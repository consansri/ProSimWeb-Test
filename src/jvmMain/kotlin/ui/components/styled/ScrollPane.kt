package me.c3.ui.components.styled

import java.awt.Component
import java.awt.Insets
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.UIManager

open class ScrollPane(uiManager: me.c3.ui.UIManager, primary: Boolean) : JScrollPane(), FocusListener {

    constructor(uiManager: me.c3.ui.UIManager, component: Component, primary: Boolean) : this(uiManager, primary) {
        this.setViewportView(component)
    }

    init {
        SwingUtilities.invokeLater {
            uiManager.themeManager.addThemeChangeListener {
                background = if (primary) it.globalStyle.bgPrimary else it.globalStyle.bgSecondary
            }

            val currTheme = uiManager.currTheme()
            background = if (primary) currTheme.globalStyle.bgPrimary else currTheme.globalStyle.bgSecondary
            border = BorderFactory.createEmptyBorder()
            this.addFocusListener(this)
        }
    }

    // Custom method to set whether the JTextPane should paint its focus state
    fun setFocusPainted(painted: Boolean) {
        border = if (painted) {
            UIManager.getBorder("ScrollPane.border")
        } else {
            BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
        }
    }

    // FocusListener implementation
    override fun focusGained(e: FocusEvent?) {
        // Customize appearance when JTextPane gains focus
        border = UIManager.getBorder("ScrollPane.border")
    }

    override fun focusLost(e: FocusEvent?) {
        // Customize appearance when JTextPane loses focus
        border = BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
    }


}