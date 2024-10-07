package prosim.ide.editor.code

import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
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
    private val rootAnnotation = Annotation(0..0, editor.file.name, Severity.INFO)

    fun updateNotations() {
        val root = DefaultMutableTreeNode(rootAnnotation)
        editor.annotations.forEach {
            root.add(NotationNode(it, editor))
        }

        val treeModel = DefaultTreeModel(root)
        setViewportView(CTree(treeModel, FontType.BASIC, notationInformationProvider, Annotation::class).apply {
            addMouseListener(MouseListener())
        })
    }

    inner class NotationInformationProvider : NodeInformationProvider<Annotation> {
        override fun getIcon(userObject: Annotation): FlatSVGIcon? {
            if (userObject == rootAnnotation) return null
            return when (userObject.severity) {
                Severity.INFO -> UIStates.icon.get().info
                Severity.WARNING -> null
                Severity.ERROR -> UIStates.icon.get().statusError
            }
        }

        override fun getName(userObject: Annotation): String {
            if (userObject == rootAnnotation) return editor.file.name
            return when (userObject.severity) {
                Severity.INFO -> "Info: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
                Severity.WARNING -> "Warning: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
                Severity.ERROR -> "Error: ${userObject.message} :${editor.textModel.getLineAndColumn(userObject.range.first)}"
            }
        }

        override fun getFgColor(userObject: Annotation): Color? {
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
            val annotation = node.userObject as Annotation

            if (e.clickCount == 2) {
                editor.selector.moveCaretTo(annotation.range.first, false)
                editor.repaint()
            }

            if (SwingUtilities.isRightMouseButton(e)) {
                tree.selectionPaths?.let { paths -> showContextMenu(tree, e.x, e.y, annotation) }
            }
        }

        private fun showContextMenu(component: Component, x: Int, y: Int, annotation: Annotation) {
            val menu = CPopupMenu()

            menu.add(CMenuItem("Locate").apply {
                addActionListener {
                    editor.selector.moveCaretTo(annotation.range.first, false)
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

    class NotationNode(val annotation: Annotation, val editor: CodeEditor) : DefaultMutableTreeNode(annotation)

}