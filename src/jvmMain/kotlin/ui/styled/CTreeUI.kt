package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTree
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class CTreeUI(uiManager: UIManager) : BasicTreeUI() {

    var iconSize: Int = 16
        set(value) {
            field = value
            tree.repaint()
        }
    var selectedColor: Color = Color(214, 217, 223)
        set(value) {
            field = value
            tree.repaint()
        }
    var expandIcon = uiManager.icons.export.derive(iconSize, iconSize)
        set(value) {
            field = value
            tree.repaint()
        }

    var closeIcon = uiManager.icons.import.derive(iconSize, iconSize)
        set(value) {
            field = value
            tree.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(0, 0, 0, 0) // Set empty border when not focused
    }

    override fun paint(g: Graphics, c: JComponent?) {
        val g2d = g.create() as Graphics2D
        g2d.color = tree.background
        g2d.fillRect(0, 0, tree.width, tree.height)
        super.paint(g2d, c)
        g2d.dispose()

    }

    override fun paintRow(g: Graphics, clipBounds: Rectangle, insets: Insets, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        val background = if (tree.isRowSelected(row)) selectedColor else tree.background
        g.color = background
        g.fillRect(0, bounds.y, tree.width, bounds.height)
        super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
    }

    override fun paintVerticalPartOfLeg(g: Graphics?, clipBounds: Rectangle?, insets: Insets?, path: TreePath?) {
        // Do not paint horizontal line
    }

    override fun paintHorizontalPartOfLeg(g: Graphics, clipBounds: Rectangle, insets: Insets, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        // Do not paint horizontal line
    }

    override fun paintExpandControl(g: Graphics, clipBounds: Rectangle, insets: Insets, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        // Do not paint expand/collapse control
        if (!isLeaf) {
            g.color = tree.foreground

            val iconX = bounds.x - iconSize - getRightChildIndent() / 2
            val iconY = bounds.y + (bounds.height - iconSize) / 2
            expandIcon.paintIcon(tree, g, iconX, iconY)
        }
    }

    private fun paintCustomIcons(g: Graphics) {
        val tree = tree
        val pathBounds = getPathBounds(tree, tree.selectionPath)
        if (pathBounds != null) {
            val model = tree.model
            val root = model.root as? DefaultMutableTreeNode
            if (root != null) {
                val children = root.children()
                while (children.hasMoreElements()) {
                    val childNode = children.nextElement() as? DefaultMutableTreeNode
                    if (childNode != null && childNode.userObject is String && childNode.userObject == "MyCustomType") {
                        val row = getRowForPath(tree, TreePath(childNode.path))
                        if (row >= 0) {
                            val iconX = getRowX(row, 0)
                            val iconY = pathBounds.y + (pathBounds.height - expandIcon.iconHeight) / 2
                            expandIcon.paintIcon(tree, g, iconX, iconY)
                        }
                    }
                }
            }
        }
    }


}