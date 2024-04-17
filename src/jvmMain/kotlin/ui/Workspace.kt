package me.c3.ui

import emulator.kit.assembly.Compiler
import emulator.kit.toCompilerFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.styled.CTree
import me.c3.ui.styled.CMenuItem
import me.c3.ui.styled.COptionPane
import me.c3.ui.styled.CPopupMenu
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class Workspace(private val path: String, codeEditor: CodeEditor, mainManager: MainManager) {
    private val rootDir = File(path)
    private val rootNode = DefaultMutableTreeNode(TreeFile(rootDir))
    private val treeModel = DefaultTreeModel(rootNode)
    val tree: CTree

    init {
        buildFileTree(rootDir, rootNode)

        tree = CTree(mainManager.themeManager, mainManager.scaleManager, mainManager.icons, treeModel, FontType.BASIC)

        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val selRow = tree.getRowForLocation(e.x, e.y)
                    if (selRow != -1) {
                        tree.selectionPath = tree.getPathForLocation(e.x, e.y)
                    }
                }
            }

            override fun mouseClicked(e: MouseEvent) {
                val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
                val uobj = selectedNode.userObject
                if (uobj !is TreeFile) return
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(mainManager, uobj, e.x, e.y)
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (uobj.file.isFile) {
                        if (e.clickCount == 2) {
                            codeEditor.openFile(uobj.file)
                        }
                    }
                    mainManager.bBar.tagInfo.text = getFileWithProjectPath(uobj.file).formatPathForDrawing()
                }
            }
        })

        setTreeLook(mainManager.themeManager)
    }

    fun getFileWithProjectPath(file: File): String {
        return rootDir.name + file.path.removePrefix(rootDir.path)
    }

    fun String.formatPathForDrawing(): String = this.replace("/", " > ").replace("\\", " > ").replace(":", "")

    fun getAllFiles(): List<File> {
        return getAllFiles(rootDir)
    }

    fun getCompilerFiles(exclude: File): List<Compiler.CompilerFile> = getAllFiles(rootDir).filter { it != exclude && it.isFile && it.name.endsWith(".s") }.map { it.toCompilerFile() }

    private fun showContextMenu(mainManager: MainManager, treeFile: TreeFile, x: Int, y: Int) {
        val popupMenu = CPopupMenu(mainManager.themeManager, mainManager.scaleManager)

        val createFileItem = if (treeFile.file.isDirectory) CMenuItem(mainManager.themeManager, mainManager.scaleManager, "New File") else null
        val createDirItem = if (treeFile.file.isDirectory) CMenuItem(mainManager.themeManager, mainManager.scaleManager, "New Directory") else null
        val openItem = if (treeFile.file.isFile) CMenuItem(mainManager.themeManager, mainManager.scaleManager, "Open") else null
        val deleteItem = CMenuItem(mainManager.themeManager, mainManager.scaleManager, "Delete")
        val renameItem = CMenuItem(mainManager.themeManager, mainManager.scaleManager, "Rename")

        openItem?.addActionListener {
            val file = ((tree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject as? TreeFile)?.file ?: return@addActionListener
            if (file.isFile) {
                mainManager.editor.openFile(file)
            }
        }

        createDirItem?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newDirName = COptionPane.showInputDialog(mainManager.themeManager, mainManager.scaleManager, tree, "Enter directory name:").await()
                if (newDirName.isNotBlank()) {
                    val newDir = File(treeFile.file, newDirName)
                    if (newDir.mkdir()) {
                        // File created successfully
                        mainManager.setCurrWS(path)
                    } else {
                        mainManager.bBar.setError("Failed to create directory $newDirName!")
                    }
                }
            }
        }

        createFileItem?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newFileName = COptionPane.showInputDialog(mainManager.themeManager, mainManager.scaleManager, tree, "Enter file name:").await()
                if (newFileName.isNotBlank()) {
                    val newFile = File(treeFile.file, newFileName)
                    if (newFile.createNewFile()) {
                        // File created successfully
                        mainManager.setCurrWS(path)
                    } else {
                        mainManager.bBar.setError("Failed to create file $newFileName!")
                    }
                }
            }
        }

        deleteItem.addActionListener {
            if (treeFile.file.delete()) {
                // File deleted successfully
                mainManager.setCurrWS(path)
            } else {
                mainManager.bBar.setError("Failed to delete file ${treeFile.file.name}!")
            }
        }

        renameItem.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newFileName = COptionPane.showInputDialog(mainManager.themeManager, mainManager.scaleManager, tree, "Enter new file name:").await()
                if (newFileName.isNotBlank()) {
                    val newFile = File(treeFile.file.parentFile, newFileName)
                    if (treeFile.file.renameTo(newFile)) {
                        // File renamed successfully
                        mainManager.setCurrWS(path)
                    } else {
                        mainManager.bBar.setError("Failed to rename file $newFileName!")
                    }
                }
            }
        }

        createFileItem?.let { popupMenu.add(it) }
        createDirItem?.let { popupMenu.add(it) }
        openItem?.let { popupMenu.add(it) }
        popupMenu.add(deleteItem)
        popupMenu.add(renameItem)

        popupMenu.show(tree, x, y)
    }

    private fun getAllFiles(directory: File): List<File> {
        val files = mutableListOf<File>()

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                files.addAll(getAllFiles(file))
            } else {
                files.add(file)
            }
        }

        return files
    }

    private fun setTreeLook(themeManager: ThemeManager) {
        themeManager.addThemeChangeListener {
            tree.background = it.globalLaF.bgSecondary
        }
        tree.background = themeManager.curr.globalLaF.bgSecondary
        tree.isFocusable = false
    }

    private fun buildFileTree(file: File, parentNode: DefaultMutableTreeNode) {
        val files = file.listFiles() ?: return

        for (childFile in files) {
            val childNode = DefaultMutableTreeNode(TreeFile(childFile))
            parentNode.add(childNode)

            if (childFile.isDirectory) {
                buildFileTree(childFile, childNode)
            }
        }
    }

    data class TreeFile(val file: File, val displayPath: Boolean = false) {
        override fun toString(): String {
            return if (displayPath) file.path else file.name
        }
    }

}

