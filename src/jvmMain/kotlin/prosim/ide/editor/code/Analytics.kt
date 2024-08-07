package prosim.ide.editor.code

import cengine.editor.CodeEditor
import cengine.editor.annotation.Notation
import cengine.editor.annotation.Severity
import com.formdev.flatlaf.extras.FlatSVGIcon
import emulator.kit.assembler.CodeStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import prosim.ide.getFileIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.CMenuItem
import prosim.uilib.styled.CPopupMenu
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.tree.CTree
import prosim.uilib.styled.tree.NodeInformationProvider
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class Analytics(val editor: PerformantCodeEditor) : CScrollPane() {

    private val notationInformationProvider = NotationInformationProvider()
    private val rootNotation = Notation(-1..-1, editor.file.name, Severity.INFO)

    fun updateNotations() {
        val root = DefaultMutableTreeNode(rootNotation)
        editor.notations.forEach {
            root.add(NotationNode(it, editor))
        }

        val treeModel = DefaultTreeModel(root)
        setViewportView(CTree(treeModel, FontType.BASIC, notationInformationProvider).apply {
            addMouseListener(MouseListener())
        })
    }

    inner class NotationInformationProvider : NodeInformationProvider<Notation> {
        override fun getIcon(userObject: Notation): FlatSVGIcon? {
            if (userObject == rootNotation) return null
            return when (userObject.severity) {
                Severity.INFO -> UIStates.icon.get().info
                Severity.WARNING -> null
                Severity.ERROR -> UIStates.icon.get().statusError
            }
        }

        override fun getName(userObject: Notation): String {
            if (userObject == rootNotation) return editor.file.name
            return when (userObject.severity) {
                Severity.INFO -> "Info: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
                Severity.WARNING -> "Warning: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
                Severity.ERROR -> "Error: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
            }
        }

        override fun getFgColor(userObject: Notation): Color? {
            return when (userObject.severity) {
                Severity.WARNING -> UIStates.theme.get().getColor(CodeStyle.YELLOW)
                Severity.ERROR -> UIStates.theme.get().getColor(CodeStyle.RED)
                else -> null
            }
        }

        override val expandedBranchIcon: FlatSVGIcon? = editor.psiManager?.lang?.getFileIcon()
        override val collapsedBranchIcon: FlatSVGIcon? = editor.psiManager?.lang?.getFileIcon()
        override val defaultLeafIcon: FlatSVGIcon? = null
    }

    private inner class MouseListener : MouseAdapter() {
        private val overlayScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
        override fun mouseClicked(e: MouseEvent) {
            val tree = e.source as CTree<*>
            val path = tree.getPathForLocation(e.x, e.y) ?: return
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val notation = node.userObject as Notation

            if (e.clickCount == 2) {
                editor.selector.moveCaretTo(notation.range.first, false)
                editor.repaint()
            }

            if (SwingUtilities.isRightMouseButton(e)) {
                tree.selectionPaths?.let { paths -> showContextMenu(tree, e.x, e.y, notation) }
            }
        }

        private fun showContextMenu(component: Component, x: Int, y: Int, notation: Notation) {
            val menu = CPopupMenu()

            menu.add(CMenuItem("Locate").apply {
                addActionListener {
                    editor.selector.moveCaretTo(notation.range.first, false)
                    editor.repaint()
                    /*overlayScope.launch {
                        val newName = COptionPane.showInputDialog(component, "Rename File:").await()
                        if (newName.isNotEmpty()) {
                            renameNode(file.path, newName)
                        }
                    }*/
                }
            })

            menu.show(component, x, y)
            menu.requestFocus()
        }
    }

    class NotationNode(val notation: Notation, val editor: CodeEditor) : DefaultMutableTreeNode(notation)

}