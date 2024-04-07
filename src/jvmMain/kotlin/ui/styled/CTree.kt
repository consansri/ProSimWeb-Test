package me.c3.ui.components.styled

import me.c3.ui.styled.CTreeUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.io.File
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

class CTree(uiManager: me.c3.ui.UIManager, treeModel: TreeModel) : JTree(treeModel), FocusListener, UIAdapter {

    init {
        this.addFocusListener(this)


        setupUI(uiManager)
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

    data class TreeFile(val file: File) {
        override fun toString(): String {
            return file.name
        }
    }

    override fun setupUI(uiManager: me.c3.ui.UIManager) {
        setUI(CTreeUI(uiManager))

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager)
        }

        setDefaults(uiManager)
    }

    fun setDefaults(uiManager: me.c3.ui.UIManager){
        val treeUI = ui as? CTreeUI ?: return
        treeUI.selectedColor = uiManager.currTheme().globalLaF.borderColor
        background = uiManager.currTheme().globalLaF.bgSecondary
        foreground = uiManager.currTheme().textLaF.base
        font = uiManager.currTheme().textLaF.font.deriveFont(uiManager.currScale().fontScale.textSize)

    }

}