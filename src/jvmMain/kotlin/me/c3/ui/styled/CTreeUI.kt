package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.manager.ResManager
import me.c3.ui.workspace.Workspace
import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import java.awt.*
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
    var selectedColor: Color = ThemeManager.curr.globalLaF.bgPrimary
        set(value) {
            field = value
            tree.repaint()
        }

    var colorFilter: FlatSVGIcon.ColorFilter? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(
            ScaleManager.curr.borderScale.insets,
            ScaleManager.curr.borderScale.insets,
            ScaleManager.curr.borderScale.insets,
            ScaleManager.curr.borderScale.insets
        ) // Set empty border when not focused
        cTree.cellRenderer = CTreeCellRenderer()

        ThemeManager.addThemeChangeListener {
            setDefaults(cTree)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(cTree)
        }

        setDefaults(cTree)
    }

    private fun setDefaults(tree: CTree) {
        selectedColor = ThemeManager.curr.globalLaF.borderColor
        colorFilter = FlatSVGIcon.ColorFilter {
            ThemeManager.curr.iconLaF.iconFgPrimary
        }
        tree.background = ThemeManager.curr.globalLaF.bgSecondary
        tree.foreground = ThemeManager.curr.textLaF.base
        tree.font = fontType.getFont()
        tree.border = ScaleManager.curr.borderScale.getInsetBorder()
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
            val loadedIcon = (if (isExpanded) ResManager.icons.folderOpen else ResManager.icons.folderClosed).derive(
                ScaleManager.curr.controlScale.smallSize,
                ScaleManager.curr.controlScale.smallSize
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
            this.textNonSelectionColor = ThemeManager.curr.textLaF.base
            this.textSelectionColor = ThemeManager.curr.textLaF.selected
            this.border = ScaleManager.curr.controlScale.getNormalInsetBorder()
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
                        ResManager.icons.asmFile.derive(
                            ScaleManager.curr.controlScale.smallSize,
                            ScaleManager.curr.controlScale.smallSize
                        )
                    } else {
                        ResManager.icons.file.derive(
                            ScaleManager.curr.controlScale.smallSize,
                            ScaleManager.curr.controlScale.smallSize
                        )
                    }
                } else {
                    ResManager.icons.folder.derive(
                        ScaleManager.curr.controlScale.smallSize,
                        ScaleManager.curr.controlScale.smallSize
                    )
                }

            } else {
                if (expanded) {
                    ResManager.icons.folder.derive(
                        ScaleManager.curr.controlScale.smallSize,
                        ScaleManager.curr.controlScale.smallSize
                    )
                } else {
                    ResManager.icons.folder.derive(
                        ScaleManager.curr.controlScale.smallSize,
                        ScaleManager.curr.controlScale.smallSize
                    )
                }
            }
            this.background = if (sel) ThemeManager.curr.textLaF.selected else ThemeManager.curr.globalLaF.bgSecondary
            loadedIcon.colorFilter = colorFilter
            this.foreground = ThemeManager.curr.textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}