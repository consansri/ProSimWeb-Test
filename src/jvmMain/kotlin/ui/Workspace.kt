package me.c3.ui

import emulator.kit.assembly.Compiler
import emulator.kit.toCompilerFile
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.styled.CTree
import me.c3.ui.theme.ThemeManager
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class Workspace(private val path: String, codeEditor: CodeEditor, uiManager: UIManager) {
    private val rootDir = File(path)
    private val rootNode = DefaultMutableTreeNode(TreeFile(rootDir))
    private val treeModel = DefaultTreeModel(rootNode)
    val tree: CTree

    init {
        val treeModel = DefaultTreeModel(rootNode)

        buildFileTree(rootDir, rootNode)

        tree = CTree(uiManager.themeManager, uiManager.scaleManager, uiManager.icons, treeModel)

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
                    val uobj = selectedNode.userObject

                    if (uobj is TreeFile) {
                        showContextMenu(uiManager, uobj, e.x, e.y)
                    }
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.clickCount == 2) {
                        val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
                        val uobj = selectedNode.userObject

                        if (uobj is TreeFile && uobj.file.isFile) {
                            codeEditor.openFile(uobj.file)
                        }
                    }
                }
            }
        })

        setTreeLook(uiManager.themeManager)
    }

    fun getAllFiles(): List<File> {
        return getAllFiles(rootDir)
    }

    fun getCompilerFiles(exclude: File): List<Compiler.CompilerFile> = getAllFiles(rootDir).filter { it != exclude && it.isFile && it.name.endsWith(".s") }.map { it.toCompilerFile() }

    private fun showContextMenu(uiManager: UIManager, treeFile: TreeFile, x: Int, y: Int) {
        val popupMenu = JPopupMenu()

        val createFileItem = if (treeFile.file.isDirectory) JMenuItem("New File") else null
        val createDirItem = if (treeFile.file.isDirectory) JMenuItem("New Directory") else null
        val deleteItem = JMenuItem("Delete")
        val renameItem = JMenuItem("Rename")

        createDirItem?.addActionListener {
            val newDirName = JOptionPane.showInputDialog("Enter directory name:", "")
            if (newDirName != null && newDirName.isNotBlank()) {
                val newDir = File(treeFile.file, newDirName)
                if (newDir.mkdir()) {
                    // File created successfully
                    uiManager.setCurrWS(path)
                } else {
                    uiManager.bBar.setError("Failed to create directory $newDirName!")
                }
            }
        }

        createFileItem?.addActionListener {
            val newFileName = JOptionPane.showInputDialog("Enter file name:", "")
            if (newFileName != null && newFileName.isNotBlank()) {
                val newFile = File(treeFile.file, newFileName)
                if (newFile.createNewFile()) {
                    // File created successfully
                    uiManager.setCurrWS(path)
                } else {
                    uiManager.bBar.setError("Failed to create file $newFileName!")
                }
            }
        }

        deleteItem.addActionListener {
            if (treeFile.file.delete()) {
                // File deleted successfully
                uiManager.setCurrWS(path)
            } else {
                uiManager.bBar.setError("Failed to delete file ${treeFile.file.name}!")
            }
        }

        renameItem.addActionListener {
            val newFileName = JOptionPane.showInputDialog("Enter new file name:", "")
            if (newFileName != null && newFileName.isNotBlank()) {
                val newFile = File(treeFile.file.parentFile, newFileName)
                if (treeFile.file.renameTo(newFile)) {
                    // File renamed successfully
                    uiManager.setCurrWS(path)
                } else {
                    uiManager.bBar.setError("Failed to rename file $newFileName!")
                }
            }
        }

        createFileItem?.let { popupMenu.add(it) }
        createDirItem?.let { popupMenu.add(it) }
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

    data class TreeFile(val file: File) {
        override fun toString(): String {
            return file.name
        }
    }

    enum class OptionMode {
        FILE,
        DIRECTORY
    }

}

