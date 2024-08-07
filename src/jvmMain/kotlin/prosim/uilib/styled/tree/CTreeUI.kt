package prosim.uilib.styled.tree

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.params.IconSize
import java.awt.*
import java.awt.geom.Path2D
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreePath

class CTreeUI<T>(val nodeInformationProvider: NodeInformationProvider<T>) : BasicTreeUI() {
    val colorFilter: FlatSVGIcon.ColorFilter
        get() = FlatSVGIcon.ColorFilter {
            if (it == Color.black) {
                UIStates.theme.get().COLOR_ICON_FG_0
            } else {
                it
            }
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree<*> ?: return
        cTree.border = BorderFactory.createEmptyBorder(
            UIStates.scale.get().SIZE_INSET_MEDIUM,
            UIStates.scale.get().SIZE_INSET_MEDIUM,
            UIStates.scale.get().SIZE_INSET_MEDIUM,
            UIStates.scale.get().SIZE_INSET_MEDIUM
        ) // Set an empty border when not focused

        cTree.cellRenderer = CNewTreeCellRenderer()
    }

    override fun paint(g: Graphics, c: JComponent?) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
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

    override fun paintRow(g: Graphics, clipBounds: Rectangle?, insets: Insets, bounds: Rectangle, path: TreePath?, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        if (tree.isRowSelected(row)) {
            val g2d = g as Graphics2D
            g2d.color = UIStates.theme.get().COLOR_SELECTION
            val dim = tree.size

            val hasNeigbourAbove = tree.selectionRows?.firstOrNull { it == row - 1 } != null
            val hasNeigbourBelow = tree.selectionRows?.firstOrNull { it == row + 1 } != null

            // Determine if the row is the first or last selected row

            // Determine the corner radius for each case
            val cornerRadius = UIStates.scale.get().SIZE_CORNER_RADIUS
            val topLeftRadius = if (!hasNeigbourAbove) cornerRadius else 0
            val topRightRadius = if (!hasNeigbourAbove) cornerRadius else 0
            val bottomLeftRadius = if (!hasNeigbourBelow) cornerRadius else 0
            val bottomRightRadius = if (!hasNeigbourBelow) cornerRadius else 0

            val path = Path2D.Float()
            path.moveTo(insets.left + topLeftRadius.toFloat(), bounds.y.toFloat())
            path.lineTo(dim.width - insets.right - topRightRadius.toFloat(), bounds.y.toFloat())
            path.quadTo(dim.width - insets.right.toFloat(), bounds.y.toFloat(), dim.width - insets.right.toFloat(), (bounds.y + topRightRadius).toFloat())
            path.lineTo(dim.width - insets.right.toFloat(), (bounds.y + bounds.height - bottomRightRadius).toFloat())
            path.quadTo(dim.width - insets.right.toFloat(), (bounds.y + bounds.height).toFloat(), (dim.width - insets.right - bottomRightRadius).toFloat(), (bounds.y + bounds.height).toFloat())
            path.lineTo((insets.left + bottomLeftRadius).toFloat(), (bounds.y + bounds.height).toFloat())
            path.quadTo(insets.left.toFloat(), (bounds.y + bounds.height).toFloat(), insets.left.toFloat(), (bounds.y + bounds.height - bottomLeftRadius).toFloat())
            path.lineTo(insets.left.toFloat(), (bounds.y + topLeftRadius).toFloat())
            path.quadTo(insets.left.toFloat(), bounds.y.toFloat(), (insets.left + topLeftRadius).toFloat(), bounds.y.toFloat())
            path.closePath()

            g2d.fill(path)
        }

        super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
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
                UIStates.scale.get().SIZE_CONTROL_SMALL,
                UIStates.scale.get().SIZE_CONTROL_SMALL
            )
            loadedIcon.colorFilter = colorFilter
            val iconX = bounds.x + insets.left - loadedIcon.iconWidth - getRightChildIndent() / 2
            val iconY = bounds.y + (bounds.height - loadedIcon.iconHeight) / 2
            loadedIcon.paintIcon(tree, g2d, iconX, iconY)
        }
        g2d.dispose()
    }

    inner class CNewTreeCellRenderer : TreeCellRenderer {
        val c = CLabel("", FontType.BASIC, BorderMode.SMALL, iconSize = IconSize.PRIMARY_SMALL)

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            c.isOpaque = selected

            val uobj = (value as? DefaultMutableTreeNode)?.userObject as? T

            if (uobj != null) {
                c.text = nodeInformationProvider.getName(uobj)
                c.customFG = nodeInformationProvider.getFgColor(uobj)

                val loadedIcon = if (leaf) {
                    nodeInformationProvider.getIcon(uobj) ?: nodeInformationProvider.defaultLeafIcon
                } else {
                    if (expanded) {
                        nodeInformationProvider.expandedBranchIcon
                    } else {
                        nodeInformationProvider.collapsedBranchIcon
                    }
                }
                loadedIcon?.colorFilter = colorFilter
                c.svgIcon = loadedIcon
            } else {
                c.text = value.toString()
                c.customFG = null
            }

            c.revalidate()
            c.repaint()
            return c
        }
    }
}