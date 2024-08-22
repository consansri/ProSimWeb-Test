package prosim.ide.filetree

import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.project.Project
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import com.formdev.flatlaf.extras.FlatSVGIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prosim.ide.getFileIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.CMenuItem
import prosim.uilib.styled.COptionPane
import prosim.uilib.styled.CPopupMenu
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.tree.CTree
import prosim.uilib.styled.tree.NodeInformationProvider
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class FileTree(private val project: Project) : FileTreeUI {
    private val vfs: VFileSystem = project.fileSystem
    private val root: DefaultMutableTreeNode = DefaultMutableTreeNode(vfs.root)
    private val treeModel: DefaultTreeModel = DefaultTreeModel(root)
    private val tree: CTree<VirtualFile> = CTree(treeModel, FontType.BASIC, object : NodeInformationProvider<VirtualFile> {
        override fun getIcon(userObject: VirtualFile): FlatSVGIcon? {
            return project.getLang(userObject)?.getFileIcon()
        }

        override fun getName(userObject: VirtualFile): String {
            return userObject.name
        }

        override fun getFgColor(userObject: VirtualFile): Color? {
            if (userObject.isDirectory && userObject.name.startsWith(".")) {
                return UIStates.theme.get().COLOR_YELLOW
            }
            return null
        }

        override val expandedBranchIcon: FlatSVGIcon = UIStates.icon.get().folder
        override val collapsedBranchIcon: FlatSVGIcon = UIStates.icon.get().folder
        override val defaultLeafIcon: FlatSVGIcon = UIStates.icon.get().file
    }, VirtualFile::class)
    private var listener: FileTreeUIListener? = null
    private val overlayScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    companion object {
        const val PLACEHOLDER_LOADING = "Loading..."
    }

    init {
        setupTree()
        loadChildren(root)
        watchFileSystem()
        tree.expandRow(0)
    }

    fun createContainer(): JComponent {
        val cScrollPane = CScrollPane(tree, true)
        return cScrollPane
    }

    private fun watchFileSystem() {
        vfs.addChangeListener(object : FileChangeListener {
            override fun onFileChanged(file: VirtualFile) {}

            override fun onFileCreated(file: VirtualFile) {
                val parent = file.parent ?: return refresh()
                val parentNode = findNodeByPath(parent.path) ?: return refresh()

                val newNode = DefaultMutableTreeNode(file)
                treeModel.insertNodeInto(newNode, parentNode, parentNode.childCount)
            }

            override fun onFileDeleted(file: VirtualFile) {
                val node = findNodeByPath(file.path)
                if (node != null) {
                    treeModel.removeNodeFromParent(node)
                }
            }
        })
    }

    private fun setupTree() {
        tree.isRootVisible = true
        tree.showsRootHandles = true
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

    override fun setFileTreeListener(listener: FileTreeUIListener) {
        this.listener = listener
    }

    private fun loadChildren(node: DefaultMutableTreeNode) {
        val uobj = node.userObject
        if (uobj is VirtualFile && uobj.isDirectory) {
            val children = uobj.getChildren()
            for (child in children) {
                val childNode = DefaultMutableTreeNode(child)
                if (child.isDirectory) {
                    // Add a dummy node to show expand icon
                    childNode.add(DefaultMutableTreeNode(DummyFile()))
                }
                node.add(childNode)
            }
            treeModel.nodeStructureChanged(node)
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
            node.removeAllChildren()
            loadChildren(node)
        }
    }

    /**
     * Displays a context menu with various file operations.
     *
     * @param x The x-coordinate of the context menu location.
     * @param y The y-coordinate of the context menu location.
     * @param allSelectedNodes All selected tree nodes.
     */
    private fun showContextMenu(x: Int, y: Int, allSelectedNodes: Collection<DefaultMutableTreeNode>) {
        val treeFiles = allSelectedNodes.mapNotNull { it.userObject as? VirtualFile }

        val popupMenu = CPopupMenu()

        val dirs = treeFiles.filter { it.isDirectory }
        val files = treeFiles.filter { it.isFile }
        val langs = treeFiles.mapNotNull { project.getLang(it) }.toSet()

        val containsDirs = dirs.isNotEmpty()
        val containsFiles = files.isNotEmpty()

        /**
         * Initialize Menu Items
         */

        // Only Directory
        val createFileItem = if (containsDirs) CMenuItem("New File") else null
        val createDirItem = if (containsDirs) CMenuItem("New Directory") else null
        val reloadFromDisk = if (containsDirs) CMenuItem("Reload") else null

        // Only File
        val openItem = if (containsFiles) CMenuItem("Open") else null

        // General
        val renameItem = CMenuItem("Rename")
        val deleteItem = CMenuItem("Delete")

        /**
         * Implement Menu Actions
         */

        // Directories

        createDirItem?.addActionListener {
            overlayScope.launch {
                val newDirName = COptionPane.showInputDialog(tree, "Enter directory name:").await()
                if (newDirName.isNotBlank()) {
                    dirs.forEach {
                        createNode(it.path, newDirName, true)
                    }
                }
            }
        }

        createFileItem?.addActionListener {
            overlayScope.launch {
                val newFileName = COptionPane.showInputDialog(tree, "Enter file name:").await()
                if (newFileName.isNotBlank()) {
                    dirs.forEach {
                        createNode(it.path, newFileName, false)
                    }
                }
            }
        }

        reloadFromDisk?.addActionListener {
            overlayScope.launch {
                refreshNode(root)

                withContext(Dispatchers.Main) {
                    tree.revalidate()
                    tree.repaint()
                }
            }
        }

        // Files

        openItem?.addActionListener {
            files.forEach {
                openFile(it.path)
            }
        }

        val runConfigs = mutableListOf<CMenuItem>()

        for (lang in langs) {
            lang.runConfigurations.forEach { config ->
                when (config) {
                    is RunConfiguration.FileRun<LanguageService> -> {
                        runConfigs += CMenuItem(config.name).apply {
                            addActionListener {
                                overlayScope.launch {
                                    treeFiles.forEach { file ->
                                        if (file.name.endsWith(lang.fileSuffix)) {
                                            config.run(file, lang, vfs)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // General

        renameItem.addActionListener {
            overlayScope.launch {
                val newFileName = COptionPane.showInputDialog(tree, "Enter new file name:").await()
                if (newFileName.isNotBlank()) {
                    treeFiles.forEach {
                        renameNode(it.path, newFileName)
                    }
                }
            }
        }

        deleteItem.addActionListener {
            overlayScope.launch {
                var dirsAreEmpty = true
                for (dir in dirs) {
                    val isEmpty = dir.getChildren().size == 0
                    if (!isEmpty) {
                        dirsAreEmpty = false
                        break
                    }
                }

                val fileInfoString = when {
                    dirs.isNotEmpty() && files.isNotEmpty() -> "${dirs.size} directories ${if (!dirsAreEmpty) "(not empty)" else ""} and ${files.size} files"
                    dirs.isNotEmpty() -> "${dirs.size} directories ${if (!dirsAreEmpty) "(not empty)" else ""}"
                    files.isNotEmpty() -> "${files.size} files"
                    else -> "nothing"
                }

                if (files.isEmpty() && dirsAreEmpty) {
                    dirs.forEach {
                        deleteNode(it.path)
                    }

                    // Files deleted successfully
                    return@launch
                }

                val confirmation = COptionPane.confirm(
                    tree,
                    "You are about to delete $fileInfoString"
                ).await()

                if (confirmation) {
                    files.forEach {
                        deleteNode(it.path)
                    }

                    dirs.forEach {
                        deleteNode(it.path)
                    }

                    // Files deleted successfully
                }
            }
        }

        /**
         * Append Menu Items
         */

        // Directory
        createFileItem?.let { popupMenu.add(it) }
        createDirItem?.let { popupMenu.add(it) }
        reloadFromDisk?.let { popupMenu.add(it) }

        // File
        openItem?.let { popupMenu.add(it) }
        runConfigs.forEach {
            popupMenu.add(it)
        }

        // Append Custom Items TODO

        // General
        popupMenu.add(renameItem)
        popupMenu.add(deleteItem)

        popupMenu.show(tree, x, y)
    }

    private class DummyFile : VirtualFile {
        override val name: String = PLACEHOLDER_LOADING
        override val path: String = ""
        override val isDirectory: Boolean = false
        override val parent: VirtualFile? = null
        override var onDiskChange: () -> Unit = { }

        override fun getChildren(): List<VirtualFile> = emptyList()

        override fun getContent(): ByteArray = ByteArray(0)

        override fun setContent(content: ByteArray) {}

        override fun toString(): String = name
    }

    private inner class TreeMouseListener : MouseAdapter() {

        override fun mouseClicked(e: MouseEvent) {
            val path = tree.getPathForLocation(e.x, e.y) ?: return
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val file = node.userObject as VirtualFile

            if (e.clickCount == 2 && !file.isDirectory) {
                openFile(file.path)
            }

            if (SwingUtilities.isRightMouseButton(e)) {
                tree.selectionPaths?.let { paths -> showContextMenu(e.x, e.y, paths.map { it.lastPathComponent as DefaultMutableTreeNode }) }
            }
        }

    }

    private inner class ExpansionListener : TreeExpansionListener {
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