package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.workspace.Workspace
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreePath

class CTreeUI : BasicTreeUI() {
    val colorFilter: FlatSVGIcon.ColorFilter
        get() = FlatSVGIcon.ColorFilter {
            UIStates.theme.get().iconLaF.iconFgPrimary
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(
            UIStates.scale.get().borderScale.insets,
            UIStates.scale.get().borderScale.insets,
            UIStates.scale.get().borderScale.insets,
            UIStates.scale.get().borderScale.insets
        ) // Set an empty border when not focused

        cTree.cellRenderer = CTreeCellRenderer()
    }

    override fun paint(g: Graphics, c: JComponent?) {
        val g2d = g.create() as Graphics2D
        g2d.color = tree.background
        g2d.fillRect(0, 0, tree.width, tree.height)
        super.paint(g2d, c)
        g2d.dispose()
    }

    override fun paintVerticalPartOfLeg(g: Graphics?, clipBounds: Rectangle?, insets: Insets?, path: TreePath?) {
        // Do not paint a horizontal line
    }

    override fun paintHorizontalPartOfLeg(
        g: Graphics,
        clipBounds: Rectangle,
        insets: Insets,
        bounds: Rectangle,
        path: TreePath,
        row: Int,
        isExpanded: Boolean,
        hasBeenExpanded: Boolean,
        isLeaf: Boolean
    ) {
        // Do not paint a horizontal line
    }

    override fun paintExpandControl(
        g: Graphics,
        clipBounds: Rectangle?,
        insets: Insets,
        bounds: Rectangle,
        path: TreePath?,
        row: Int,
        isExpanded: Boolean,
        hasBeenExpanded: Boolean,
        isLeaf: Boolean
    ) {
        val g2d = g.create() as? Graphics2D ?: return
        if (!isLeaf) {
            val loadedIcon = (if (isExpanded) UIStates.icon.get().folderOpen else UIStates.icon.get().folderClosed).derive(
                UIStates.scale.get().controlScale.smallSize,
                UIStates.scale.get().controlScale.smallSize
            )
            loadedIcon.colorFilter = colorFilter
            val iconX = bounds.x + insets.left - loadedIcon.iconWidth - getRightChildIndent() / 2
            val iconY = bounds.y + (bounds.height - loadedIcon.iconHeight) / 2
            loadedIcon.paintIcon(tree, g2d, iconX, iconY)
        }
        g2d.dispose()
    }

    inner class CTreeCellRenderer : DefaultTreeCellRenderer() {

        init {
            this.isOpaque = true
            this.font = tree.font
            this.textNonSelectionColor = UIStates.theme.get().textLaF.base
            this.textSelectionColor = UIStates.theme.get().textLaF.selected
            this.border = UIStates.scale.get().controlScale.getNormalInsetBorder()
        }

        override fun getTreeCellRendererComponent(
            tree: JTree?,
            value: Any?,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            val uobj = ((value as? DefaultMutableTreeNode)?.userObject as? Workspace.TreeFile)

            val loadedIcon = if (leaf) {
                if (uobj != null && uobj.file.isFile) {
                    if (uobj.file.extension == "s") {
                        UIStates.icon.get().asmFile.derive(
                            UIStates.scale.get().controlScale.smallSize,
                            UIStates.scale.get().controlScale.smallSize
                        )
                    } else {
                        UIStates.icon.get().file.derive(
                            UIStates.scale.get().controlScale.smallSize,
                            UIStates.scale.get().controlScale.smallSize
                        )
                    }
                } else {
                    UIStates.icon.get().folder.derive(
                        UIStates.scale.get().controlScale.smallSize,
                        UIStates.scale.get().controlScale.smallSize
                    )
                }

            } else {
                if (expanded) {
                    UIStates.icon.get().folder.derive(
                        UIStates.scale.get().controlScale.smallSize,
                        UIStates.scale.get().controlScale.smallSize
                    )
                } else {
                    UIStates.icon.get().folder.derive(
                        UIStates.scale.get().controlScale.smallSize,
                        UIStates.scale.get().controlScale.smallSize
                    )
                }
            }
            this.background = if (sel) UIStates.theme.get().textLaF.selected else UIStates.theme.get().globalLaF.bgSecondary
            loadedIcon.colorFilter = colorFilter
            this.foreground = UIStates.theme.get().textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}