package prosim.ide.filetree

import cengine.project.Project
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import cengine.vfs.tree.FileTreeUI
import cengine.vfs.tree.FileTreeUIChangeListener
import com.formdev.flatlaf.extras.FlatSVGIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.ide.getFileIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.CMenuItem
import prosim.uilib.styled.COptionPane
import prosim.uilib.styled.CPopupMenu
import prosim.uilib.styled.CTree
import prosim.uilib.styled.params.FontType
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class FileTree(private val project: Project) : FileTreeUI {
    private val vfs: VFileSystem = project.fileSystem
    private val root: DefaultMutableTreeNode
    private val treeModel: DefaultTreeModel
    val tree: CTree
    private var listener: FileTreeUIChangeListener? = null

    companion object {
        val PLACEHOLDER_LOADING = "Loading..."
    }

    init {
        root = DefaultMutableTreeNode(vfs.root)
        treeModel = DefaultTreeModel(root)
        tree = CTree(treeModel, FontType.BASIC)
        setupTree()
        loadChildren(root)
    }

    private fun setupTree() {
        tree.isRootVisible = true
        tree.showsRootHandles = true
        tree.cellRenderer = CellRenderer()
        tree.addMouseListener(TreeMouseListener())
        tree.addTreeExpansionListener(ExpansionListener())
    }

    override fun refresh() {
        refreshNode(root)
        treeModel.reload()
    }


    override fun expandNode(path: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            val treePath = TreePath(node.path)
            tree.expandPath(treePath)
        }
    }


    override fun collapseNode(path: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            val treePath = TreePath(node.path)
            tree.collapsePath(treePath)
        }
    }

    override fun selectNode(path: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            val treePath = TreePath(node.path)
            tree.selectionPath = treePath
        }
    }

    override fun createNode(parentPath: String, name: String, isDirectory: Boolean) {
        val parentNode = findNodeByPath(parentPath)
        if (parentNode != null) {
            val newFile = vfs.createFile("$parentPath${VFileSystem.DELIMITER}$name", isDirectory)
            val newNode = DefaultMutableTreeNode(newFile)
            treeModel.insertNodeInto(newNode, parentNode, parentNode.childCount)
            listener?.onCreateRequest(parentNode.userObject as VirtualFile, name, isDirectory)
        }
    }

    override fun deleteNode(path: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            treeModel.removeNodeFromParent(node)
            vfs.deleteFile(path)
            listener?.onDeleteRequest(node.userObject as VirtualFile)
        }
    }

    override fun renameNode(path: String, newName: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            val file = node.userObject as VirtualFile
            val newPath = "${file.parent?.path ?: ""}${VFileSystem.DELIMITER}$newName"
            val newFile = vfs.createFile(newPath, file.isDirectory)
            if (!file.isDirectory) {
                newFile.setContent(file.getContent())
            }
            vfs.deleteFile(path)
            node.userObject = newFile
            treeModel.nodeChanged(node)
            listener?.onRenameRequest(node.userObject as VirtualFile, newName)
        }
    }

    override fun openFile(path: String) {
        val node = findNodeByPath(path)
        if (node != null) {
            listener?.onOpenRequest(node.userObject as VirtualFile)
        }
    }

    override fun setFileTreeListener(listener: FileTreeUIChangeListener) {
        this.listener = listener
    }

    private fun loadChildren(node: DefaultMutableTreeNode) {
        val uobj = node.userObject
        if (uobj is VirtualFile && uobj.isDirectory) {
            val children = uobj.getChildren()
            for (child in children) {
                val childNode = DefaultMutableTreeNode(child)
                node.add(childNode)
                if (child.isDirectory) {
                    // Add a dummy node to show expand icon
                    childNode.add(DefaultMutableTreeNode(DummyFile()))
                }
            }
        }
    }

    private fun findNodeByPath(path: String): DefaultMutableTreeNode? {
        fun search(node: DefaultMutableTreeNode): DefaultMutableTreeNode? {
            val uobj = node.userObject
            if (uobj is VirtualFile && uobj.path == path) return node
            for (i in 0 until node.childCount) {
                val child = node.getChildAt(i) as DefaultMutableTreeNode
                val result = search(child)
                if (result != null) return result
            }
            return null
        }
        return search(root)
    }

    private fun refreshNode(node: DefaultMutableTreeNode) {
        val file = node.userObject as VirtualFile
        if (file.isDirectory) {
            val expandedPaths = tree.getExpandedDescendants(TreePath(node.path))
            node.removeAllChildren()
            loadChildren(node)
            expandedPaths?.asSequence()?.forEach { tree.expandPath(it as TreePath) }
        }
    }

    private class DummyFile: VirtualFile{
        override val name: String = PLACEHOLDER_LOADING
        override val path: String = ""
        override val isDirectory: Boolean = false
        override val parent: VirtualFile? = null
        override var onDiskChange: () -> Unit = {

        }

        override fun getChildren(): List<VirtualFile> = emptyList()

        override fun getContent(): ByteArray = ByteArray(0)

        override fun setContent(content: ByteArray) {}

        override fun toString(): String = name
    }

    private inner class TreeMouseListener() : MouseAdapter() {
        val overlayScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
        override fun mouseClicked(e: MouseEvent) {
            val path = tree.getPathForLocation(e.x, e.y) ?: return
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val file = node.userObject as VirtualFile

            if (e.clickCount == 2 && !file.isDirectory) {
                openFile(file.path)
            }

            if (e.isPopupTrigger) {
                showContextMenu(tree, e.x, e.y, file)
            }
        }

        private fun showContextMenu(component: java.awt.Component, x: Int, y: Int, file: VirtualFile) {
            val menu = CPopupMenu()

            if (file.isDirectory) {
                menu.add(CMenuItem("New File").apply {
                    addActionListener {
                        overlayScope.launch {
                            val newName = COptionPane.showInputDialog(component, "Create New File:").await()
                            if (newName.isNotEmpty()) {
                                createNode(file.path, newName, false)
                            }
                        }
                    }
                })
                menu.add(CMenuItem("New Directory").apply {
                    addActionListener {
                        overlayScope.launch {
                            val newName = COptionPane.showInputDialog(component, "Create New Directory:").await()
                            if (newName.isNotEmpty()) {
                                createNode(file.path, newName, true)
                            }
                        }
                    }
                })
            }

            menu.add(CMenuItem("Rename").apply {
                addActionListener {
                    overlayScope.launch {
                        val newName = COptionPane.showInputDialog(component, "Rename File:").await()
                        if (newName.isNotEmpty()) {
                            renameNode(file.path, newName)
                        }
                    }
                }
            })

            menu.show(component, x, y)
        }
    }

    private inner class CellRenderer : DefaultTreeCellRenderer() {

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

            val uobj = ((value as? DefaultMutableTreeNode)?.userObject as? VFileSystem.VirtualFileImpl)

            val loadedIcon = if (leaf) {
                if (uobj != null && !uobj.isDirectory) {
                    val fileIcon = project.getLang(uobj)?.getFileIcon()
                    if (fileIcon != null) {
                        fileIcon.derive(
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
            loadedIcon.colorFilter = FlatSVGIcon.ColorFilter {
                UIStates.theme.get().iconLaF.iconFgPrimary
            }
            this.foreground = UIStates.theme.get().textLaF.base
            this.icon = loadedIcon
            return this
        }
    }

    private inner class ExpansionListener() : TreeExpansionListener {
        override fun treeExpanded(event: TreeExpansionEvent) {
            val node = event.path.lastPathComponent as DefaultMutableTreeNode
            if (node.childCount == 1 && (node.getChildAt(0) as DefaultMutableTreeNode).userObject is DummyFile) {
                node.removeAllChildren()
                loadChildren(node)
                treeModel.nodeStructureChanged(node)
            }
        }

        override fun treeCollapsed(event: TreeExpansionEvent?) {
            // Do nothing
        }
    }
}