package me.c3.ui.components.styled

import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.BorderFactory
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.tree.TreeModel

class CTree(treeModel: TreeModel) : JTree(treeModel), FocusListener {

    init {
        this.addFocusListener(this)
        border = BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
    }

    // Custom method to set whether the JTextPane should paint its focus state
    fun setFocusPainted(painted: Boolean) {
        border = if (painted) {
            UIManager.getBorder("Tree.border")
        } else {
            BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
        }
    }

    // FocusListener implementation
    override fun focusGained(e: FocusEvent?) {
        // Customize appearance when JTextPane gains focus
        border = UIManager.getBorder("Tree.border")
    }

    override fun focusLost(e: FocusEvent?) {
        // Customize appearance when JTextPane loses focus
        border = BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
    }

}