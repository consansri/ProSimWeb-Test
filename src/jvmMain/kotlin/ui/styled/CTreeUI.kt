package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import emulator.kit.nativeLog
import me.c3.ui.UIManager
import me.c3.ui.Workspace
import me.c3.ui.components.styled.CTree
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.ProSimIcons
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

class CTreeUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val icons: ProSimIcons) : BasicTreeUI() {
    var selectedColor: Color = themeManager.curr.globalLaF.bgPrimary
        set(value) {
            field = value
            tree.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cTree = c as? CTree ?: return
        cTree.border = BorderFactory.createEmptyBorder(scaleManager.curr.borderScale.insets, scaleManager.curr.borderScale.insets, scaleManager.curr.borderScale.insets, scaleManager.curr.borderScale.insets) // Set empty border when not focused
        cTree.cellRenderer = CTreeCellRenderer(icons)

        themeManager.addThemeChangeListener {
            setDefaults(cTree)
        }
        
        scaleManager.addScaleChangeEvent {
            setDefaults(cTree)
        }
        
        setDefaults(cTree)        
    }
    
    private fun setDefaults(tree: CTree){
        selectedColor = themeManager.curr.globalLaF.borderColor
        tree.background = themeManager.curr.globalLaF.bgSecondary
        tree.foreground = themeManager.curr.textLaF.base
        tree.font = themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
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

    override fun paintHorizontalPartOfLeg(g: Graphics, clipBounds: Rectangle, insets: Insets, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        // Do not paint horizontal line

    }

    override fun paintExpandControl(g: Graphics, clipBounds: Rectangle?, insets: Insets, bounds: Rectangle, path: TreePath?, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        val g2d = g.create() as? Graphics2D ?: return
        if (!isLeaf) {
            val loadedIcon = (if (isExpanded) icons.folderOpen else icons.folderClosed).derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
            loadedIcon.colorFilter = themeManager.curr.icon.colorFilter
            val iconX = bounds.x + insets.left - loadedIcon.iconWidth - getRightChildIndent() / 2
            val iconY = bounds.y + (bounds.height - loadedIcon.iconHeight) / 2
            loadedIcon.paintIcon(tree, g2d, iconX, iconY)
        }
        g2d.dispose()
    }

    inner class CTreeCellRenderer(icons: ProSimIcons) : DefaultTreeCellRenderer() {

        init {
            this.isOpaque = true
            this.font = themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
            this.textNonSelectionColor = themeManager.curr.textLaF.base
            this.textSelectionColor = themeManager.curr.textLaF.selelected
        }

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            val uobj = ((value as? DefaultMutableTreeNode)?.userObject as? Workspace.TreeFile)

            val loadedIcon = if (leaf) {
                if (uobj != null && uobj.file.isFile) {
                    if (uobj.file.extension == "s") {
                        icons.asmFile.derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
                    } else {
                        icons.file.derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
                    }
                } else {
                    icons.folder.derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
                }

            } else {
                if (expanded) {
                    icons.folder.derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
                } else {
                    icons.folder.derive(scaleManager.curr.controlScale.smallSize, scaleManager.curr.controlScale.smallSize)
                }
            }

            this.background = if(sel)  themeManager.curr.textLaF.selelected else themeManager.curr.globalLaF.bgSecondary
            loadedIcon.colorFilter = themeManager.curr.icon.colorFilter
            this.foreground = themeManager.curr.textLaF.base
            this.icon = loadedIcon
            return this
        }
    }
}