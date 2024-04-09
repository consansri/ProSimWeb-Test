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

class CTree(uiManager: me.c3.ui.UIManager, treeModel: TreeModel) : JTree(treeModel), UIAdapter {

    init {
        setupUI(uiManager)
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