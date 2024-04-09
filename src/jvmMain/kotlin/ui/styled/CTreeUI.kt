package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.Workspace
import me.c3.ui.components.styled.CTree
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreePath
import kotlin.math.exp

class CTreeUI(private val uiManager: UIManager) : BasicTreeUI() {
    var selectedColor: Color = Color(214, 217, 223)
        set(value) {
            field = value
            tree.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(uiManager.currScale().borderScale.insets, uiManager.currScale().borderScale.insets, uiManager.currScale().borderScale.insets, uiManager.currScale().borderScale.insets) // Set empty border when not focused
        cTree.cellRenderer = CTreeCellRenderer(uiManager)
    }

    override fun paint(g: Graphics, c: JComponent?) {
        val g2d = g.create() as Graphics2D
        g2d.color = tree.background
        g2d.fillRect(0, 0, tree.width, tree.height)
        super.paint(g2d, c)
        g2d.dispose()

    }

    override fun paintVerticalPartOfLeg(g: Graphics?, clipBounds: Rectangle?, insets: Insets?, path: TreePath?) {
        // Do not paint horizontal line
    }

    override fun paintHorizontalPartOfLeg(g: Graphics, clipBounds: Rectangle, insets: Insets, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        // Do not paint horizontal line

    }

    override fun paintExpandControl(g: Graphics, clipBounds: Rectangle?, insets: Insets, bounds: Rectangle, path: TreePath?, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {

        val g2d = g.create() as? Graphics2D ?: return
        if (!isLeaf) {
            val loadedIcon = (if (isExpanded) uiManager.icons.folderOpen else uiManager.icons.folderClosed).derive(uiManager.currScale().controlScale.smallSize, uiManager.currScale().controlScale.smallSize)
            loadedIcon.colorFilter = uiManager.currTheme().icon.colorFilter
            val iconX = bounds.x + insets.left - loadedIcon.iconWidth - getRightChildIndent() / 2
            val iconY = bounds.y + (bounds.height - loadedIcon.iconHeight) / 2
            loadedIcon.paintIcon(tree, g2d, iconX, iconY)
        }
        g2d.dispose()
    }

    inner class CTreeCellRenderer(private val uiManager: UIManager) : DefaultTreeCellRenderer() {

        init {
            this.isOpaque = true
            this.font = uiManager.currTheme().textLaF.getBaseFont().deriveFont(uiManager.currScale().fontScale.textSize)
            this.textNonSelectionColor = uiManager.currTheme().textLaF.base
            this.textSelectionColor = uiManager.currTheme().textLaF.base
        }

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            val uobj = ((value as? DefaultMutableTreeNode)?.userObject as? Workspace.TreeFile)

            val loadedIcon = if (leaf) {
                if (uobj != null && uobj.file.isFile && uobj.file.extension == "s") {
                    uiManager.icons.asmFile.derive(uiManager.currScale().controlScale.smallSize, uiManager.currScale().controlScale.smallSize)
                } else {
                    uiManager.icons.file.derive(uiManager.currScale().controlScale.smallSize, uiManager.currScale().controlScale.smallSize)
                }
            } else {
                if (expanded) {
                    uiManager.icons.folder.derive(uiManager.currScale().controlScale.smallSize, uiManager.currScale().controlScale.smallSize)
                } else {
                    uiManager.icons.folder.derive(uiManager.currScale().controlScale.smallSize, uiManager.currScale().controlScale.smallSize)
                }
            }
            loadedIcon.colorFilter = uiManager.currTheme().icon.colorFilter
            this.foreground = uiManager.currTheme().textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}