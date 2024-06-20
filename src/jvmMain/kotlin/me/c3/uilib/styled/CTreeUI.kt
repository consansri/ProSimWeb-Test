package me.c3.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.workspace.Workspace
import me.c3.uilib.UIManager
import me.c3.uilib.styled.params.FontType
import java.awt.*
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreePath

class CTreeUI(
    private val fontType: FontType
) : BasicTreeUI() {
    var selectedColor: Color = UIManager.theme.get().globalLaF.bgPrimary
        set(value) {
            field = value
            tree.repaint()
        }

    var colorFilter: FlatSVGIcon.ColorFilter? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(
            UIManager.scale.get().borderScale.insets,
            UIManager.scale.get().borderScale.insets,
            UIManager.scale.get().borderScale.insets,
            UIManager.scale.get().borderScale.insets
        ) // Set empty border when not focused
        cTree.cellRenderer = CTreeCellRenderer()

        UIManager.theme.addEvent(WeakReference(cTree)) { _ ->
            setDefaults(cTree)
        }

        UIManager.scale.addEvent(WeakReference(cTree)) { _ ->
            setDefaults(cTree)
        }

        setDefaults(cTree)
    }

    private fun setDefaults(tree: CTree) {
        selectedColor = UIManager.theme.get().globalLaF.borderColor
        colorFilter = FlatSVGIcon.ColorFilter {
            UIManager.theme.get().iconLaF.iconFgPrimary
        }
        tree.background = UIManager.theme.get().globalLaF.bgSecondary
        tree.foreground = UIManager.theme.get().textLaF.base
        tree.font = fontType.getFont()
        tree.border = UIManager.scale.get().borderScale.getInsetBorder()
        tree.revalidate()
        tree.repaint()
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
        // Do not paint horizontal line

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
            val loadedIcon = (if (isExpanded) UIManager.icon.get().folderOpen else UIManager.icon.get().folderClosed).derive(
                UIManager.scale.get().controlScale.smallSize,
                UIManager.scale.get().controlScale.smallSize
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
            this.textNonSelectionColor = UIManager.theme.get().textLaF.base
            this.textSelectionColor = UIManager.theme.get().textLaF.selected
            this.border = UIManager.scale.get().controlScale.getNormalInsetBorder()
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
                        UIManager.icon.get().asmFile.derive(
                            UIManager.scale.get().controlScale.smallSize,
                            UIManager.scale.get().controlScale.smallSize
                        )
                    } else {
                        UIManager.icon.get().file.derive(
                            UIManager.scale.get().controlScale.smallSize,
                            UIManager.scale.get().controlScale.smallSize
                        )
                    }
                } else {
                    UIManager.icon.get().folder.derive(
                        UIManager.scale.get().controlScale.smallSize,
                        UIManager.scale.get().controlScale.smallSize
                    )
                }

            } else {
                if (expanded) {
                    UIManager.icon.get().folder.derive(
                        UIManager.scale.get().controlScale.smallSize,
                        UIManager.scale.get().controlScale.smallSize
                    )
                } else {
                    UIManager.icon.get().folder.derive(
                        UIManager.scale.get().controlScale.smallSize,
                        UIManager.scale.get().controlScale.smallSize
                    )
                }
            }
            this.background = if (sel) UIManager.theme.get().textLaF.selected else UIManager.theme.get().globalLaF.bgSecondary
            loadedIcon.colorFilter = colorFilter
            this.foreground = UIManager.theme.get().textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}