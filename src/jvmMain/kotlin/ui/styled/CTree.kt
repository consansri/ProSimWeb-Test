package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CTreeUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

class CTree(themeManager: ThemeManager, scaleManager: ScaleManager, icons: ProSimIcons, treeModel: TreeModel) : JTree(treeModel) {

    init {
        setUI(CTreeUI(themeManager, scaleManager, icons))
    }
}