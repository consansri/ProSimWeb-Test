package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.Workspace
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
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
    private val themeManager: ThemeManager,
    private val scaleManager: ScaleManager,
    private val icons: ProSimIcons,
    private val fontType: FontType
) : BasicTreeUI() {
    var selectedColor: Color = themeManager.curr.globalLaF.bgPrimary
        set(value) {
            field = value
            tree.repaint()
        }

    var colorFilter: FlatSVGIcon.ColorFilter? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(
            scaleManager.curr.borderScale.insets,
            scaleManager.curr.borderScale.insets,
            scaleManager.curr.borderScale.insets,
            scaleManager.curr.borderScale.insets
        ) // Set empty border when not focused
        cTree.cellRenderer = CTreeCellRenderer()

        themeManager.addThemeChangeListener {
            setDefaults(cTree)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(cTree)
        }

        setDefaults(cTree)
    }

    private fun setDefaults(tree: CTree) {
        selectedColor = themeManager.curr.globalLaF.borderColor
        colorFilter = FlatSVGIcon.ColorFilter {
            themeManager.curr.iconLaF.iconFgPrimary
        }
        tree.background = themeManager.curr.globalLaF.bgSecondary
        tree.foreground = themeManager.curr.textLaF.base
        tree.font = fontType.getFont(themeManager, scaleManager)
        tree.border = scaleManager.curr.borderScale.getInsetBorder()
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
            val loadedIcon = (if (isExpanded) icons.folderOpen else icons.folderClosed).derive(
                scaleManager.curr.controlScale.smallSize,
                scaleManager.curr.controlScale.smallSize
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
            this.textNonSelectionColor = themeManager.curr.textLaF.base
            this.textSelectionColor = themeManager.curr.textLaF.selected
            this.border = scaleManager.curr.controlScale.getNormalInsetBorder()
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
                        icons.asmFile.derive(
                            scaleManager.curr.controlScale.smallSize,
                            scaleManager.curr.controlScale.smallSize
                        )
                    } else {
                        icons.file.derive(
                            scaleManager.curr.controlScale.smallSize,
                            scaleManager.curr.controlScale.smallSize
                        )
                    }
                } else {
                    icons.folder.derive(
                        scaleManager.curr.controlScale.smallSize,
                        scaleManager.curr.controlScale.smallSize
                    )
                }

            } else {
                if (expanded) {
                    icons.folder.derive(
                        scaleManager.curr.controlScale.smallSize,
                        scaleManager.curr.controlScale.smallSize
                    )
                } else {
                    icons.folder.derive(
                        scaleManager.curr.controlScale.smallSize,
                        scaleManager.curr.controlScale.smallSize
                    )
                }
            }
            this.background = if (sel) themeManager.curr.textLaF.selected else themeManager.curr.globalLaF.bgSecondary
            loadedIcon.colorFilter = colorFilter
            this.foreground = themeManager.curr.textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}