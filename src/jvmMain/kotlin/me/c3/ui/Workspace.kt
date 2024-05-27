package me.c3.ui

import emulator.kit.assembler.AssemblerFile
import emulator.kit.common.FileBuilder
import emulator.kit.toCompilerFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.styled.CTree
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

/**
 * Manages the workspace for the code editor, including the file tree and related UI components.
 * @param path The path to the workspace directory.
 * @param codeEditor The code editor instance.
 * @param mainManager The main manager instance.
 */
class Workspace(private val path: String, codeEditor: CodeEditor, mainManager: MainManager) {
    // Root directory of the workspace.
    private val rootDir = File(path)

    // Root node of the file tree.
    private val rootNode = DefaultMutableTreeNode(TreeFile(rootDir))

    // Tree model representing the file tree structure.
    private val treeModel = DefaultTreeModel(rootNode)

    // UI component representing the file tree.
    val tree: CTree

    init {
        // Build the file tree starting from the root directory.
        buildFileTree(rootDir, rootNode)

        // Initialize the file tree UI component.
        tree = CTree(mainManager.tm, mainManager.sm, mainManager.icons, treeModel, FontType.BASIC)

        // Add mouse listener for handling user interactions with the file tree.
        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {

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

        // Set the initial look and feel of the file tree.
        setTreeLook(mainManager.tm)
    }

    /**
     * Constructs a string representing the file path relative to the project root directory.
     * @param file The file for which to generate the relative path.
     * @return The relative file path.
     */
    fun getFileWithProjectPath(file: File): String {
        return rootDir.name + file.path.removePrefix(rootDir.path)
    }

    /**
     * Formats the file path for display by replacing certain characters.
     * @return The formatted file path string.
     */
    fun String.formatPathForDrawing(): String = this.replace("/", " > ").replace("\\", " > ").replace(":", "")

    /**
     * Retrieves all files in the workspace.
     * @return A list of all files in the workspace.
     */
    fun getAllFiles(): List<File> {
        return getAllFiles(rootDir)
    }

    /**
     * Retrieves all compiler files in the workspace, excluding a specified file.
     * @param exclude The file to exclude from the list.
     * @return A list of compiler files.
     */
    fun getCompilerFiles(exclude: File): List<AssemblerFile> = getAllFiles(rootDir).filter { it != exclude && it.isFile && (it.name.endsWith(".s") || it.name.endsWith(".S")) }.map { it.toCompilerFile() }

    /**
     * Displays a context menu with various file operations.
     * @param mainManager The main manager instance.
     * @param treeFile The file for which the context menu is shown.
     * @param x The x-coordinate of the context menu location.
     * @param y The y-coordinate of the context menu location.
     */
    private fun showContextMenu(mainManager: MainManager, treeFile: TreeFile, x: Int, y: Int) {
        val popupMenu = CPopupMenu(mainManager.tm, mainManager.sm)

        /**
         * Initialize Menu Items
         */

        // Only Directory
        val createFileItem = if (treeFile.file.isDirectory) CMenuItem(mainManager.tm, mainManager.sm, "New File") else null
        val createDirItem = if (treeFile.file.isDirectory) CMenuItem(mainManager.tm, mainManager.sm, "New Directory") else null
        val reloadFromDisk = if (treeFile.file.isDirectory) CMenuItem(mainManager.tm, mainManager.sm, "Reload") else null

        // Only File
        val openItem = if (treeFile.file.isFile) CMenuItem(mainManager.tm, mainManager.sm, "Open") else null

        // Only Assembly File
        val buildFile = if (treeFile.file.isFile && treeFile.file.name.endsWith(".s")) CMenuItem(mainManager.tm, mainManager.sm, "Build") else null
        val exportMIF = if (treeFile.file.isFile && treeFile.file.name.endsWith(".s")) CMenuItem(mainManager.tm, mainManager.sm, "Generate MIF") else null
        val exportHexDump = if (treeFile.file.isFile && treeFile.file.name.endsWith(".s")) CMenuItem(mainManager.tm, mainManager.sm, "Generate HexDump") else null
        val exportVHDL = if (treeFile.file.isFile && treeFile.file.name.endsWith(".s")) CMenuItem(mainManager.tm, mainManager.sm, "Generate VHDL") else null
        val exportTS = if (treeFile.file.isFile && treeFile.file.name.endsWith(".s")) CMenuItem(mainManager.tm, mainManager.sm, "Generate Transcript") else null

        // General
        val renameItem = CMenuItem(mainManager.tm, mainManager.sm, "Rename")
        val deleteItem = CMenuItem(mainManager.tm, mainManager.sm, "Delete")

        /**
         * Implement Menu Actions
         */

        // Directories

        createDirItem?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newDirName = COptionPane.showInputDialog(mainManager.tm, mainManager.sm, tree, "Enter directory name:").await()
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
                val newFileName = COptionPane.showInputDialog(mainManager.tm, mainManager.sm, tree, "Enter file name:").await()
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

        reloadFromDisk?.addActionListener {
            CoroutineScope(Dispatchers.IO).launch {
                rootNode.removeAllChildren()
                buildFileTree(rootDir, rootNode)
            }
        }

        // Files

        openItem?.addActionListener {
            val files = tree.selectionPaths?.mapNotNull { (it.lastPathComponent as? DefaultMutableTreeNode)?.userObject as? TreeFile }
            files?.forEach {
                if(it.file.isFile){
                    mainManager.editor.openFile(it.file)
                }
            }
        }

        buildFile?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainManager.currArch().compile(treeFile.file.toCompilerFile(), getCompilerFiles(treeFile.file), true)
                mainManager.eventManager.triggerCompileFinished(result)
            }
        }

        exportMIF?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainManager.currArch().compile(treeFile.file.toCompilerFile(), getCompilerFiles(treeFile.file), true)
                mainManager.eventManager.triggerCompileFinished(result)
                if (!result.success) {
                    return@launch
                }
                val fileContent = mainManager.currArch().getFormattedFile(FileBuilder.ExportFormat.MIF, treeFile.file.toCompilerFile())
                val generatedDir = File(treeFile.file.parentFile, ".generated")
                if (!generatedDir.exists()) {
                    generatedDir.mkdir()
                }
                val newFile = File(generatedDir, treeFile.file.name.removeSuffix(".s") + ".mif")
                if (newFile.createNewFile()) {
                    newFile.writeText(fileContent.joinToString("\n") { it })
                    // File build successfully
                    mainManager.setCurrWS(path)
                } else {
                    mainManager.bBar.setError("Failed to save generated file ${newFile.name}!")
                }
            }
        }

        exportVHDL?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainManager.currArch().compile(treeFile.file.toCompilerFile(), getCompilerFiles(treeFile.file), true)
                mainManager.eventManager.triggerCompileFinished(result)
                if (!result.success) {
                    return@launch
                }
                val fileContent = mainManager.currArch().getFormattedFile(FileBuilder.ExportFormat.VHDL, treeFile.file.toCompilerFile())
                val generatedDir = File(treeFile.file.parentFile, ".generated")
                if (!generatedDir.exists()) {
                    generatedDir.mkdir()
                }

                val newFile = File(generatedDir, treeFile.file.name.removeSuffix(".s") + ".vhd")
                if (newFile.createNewFile()) {
                    newFile.writeText(fileContent.joinToString("\n") { it })
                    // File build successfully
                    mainManager.setCurrWS(path)
                } else {
                    mainManager.bBar.setError("Failed to save generated file ${newFile.name}!")
                }
            }
        }

        exportHexDump?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainManager.currArch().compile(treeFile.file.toCompilerFile(), getCompilerFiles(treeFile.file), true)
                mainManager.eventManager.triggerCompileFinished(result)
                if (!result.success) {
                    return@launch
                }
                val fileContent = mainManager.currArch().getFormattedFile(FileBuilder.ExportFormat.HEXDUMP, treeFile.file.toCompilerFile())
                val generatedDir = File(treeFile.file.parentFile, ".generated")
                if (!generatedDir.exists()) {
                    generatedDir.mkdir()
                }

                mainManager.eventManager.triggerCompileFinished(result)

                val newFile = File(generatedDir, treeFile.file.name.removeSuffix(".s") + ".hexdump")
                if (newFile.createNewFile()) {
                    newFile.writeText(fileContent.joinToString("\n") { it })
                    // File build successfully
                    mainManager.setCurrWS(path)
                } else {
                    mainManager.bBar.setError("Failed to save generated file ${newFile.name}!")
                }
            }
        }

        exportTS?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainManager.currArch().compile(treeFile.file.toCompilerFile(), getCompilerFiles(treeFile.file), true)
                mainManager.eventManager.triggerCompileFinished(result)
                if (!result.success) {
                    return@launch
                }
                val fileContent = result.generateTS()
                val generatedDir = File(treeFile.file.parentFile, ".generated")
                if (!generatedDir.exists()) {
                    generatedDir.mkdir()
                }

                val newFile = File(generatedDir, treeFile.file.name.removeSuffix(".s") + ".transcript")
                if (newFile.createNewFile()) {
                    newFile.writeText(fileContent)
                    // File build successfully
                    mainManager.setCurrWS(path)
                } else {
                    mainManager.bBar.setError("Failed to save generated file ${newFile.name}!")
                }
            }
        }

        // General

        renameItem.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newFileName = COptionPane.showInputDialog(mainManager.tm, mainManager.sm, tree, "Enter new file name:").await()
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

        deleteItem.addActionListener {
            if (treeFile.file.delete()) {
                // File deleted successfully
                mainManager.setCurrWS(path)
            } else {
                mainManager.bBar.setError("Failed to delete file ${treeFile.file.name}!")
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

        buildFile?.let { popupMenu.add(it) }
        exportMIF?.let { popupMenu.add(it) }
        exportVHDL?.let { popupMenu.add(it) }
        exportHexDump?.let { popupMenu.add(it) }
        exportTS?.let { popupMenu.add(it) }

        // General
        popupMenu.add(renameItem)
        popupMenu.add(deleteItem)

        popupMenu.show(tree, x, y)
    }

    /**
     * Recursively retrieves all files from the specified directory.
     * @param directory The root directory to search.
     * @return A list of all files in the directory.
     */
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

    /**
     * Sets the visual appearance of the file tree based on the current theme.
     * @param tm The theme manager instance.
     */
    private fun setTreeLook(tm: ThemeManager) {
        tm.addThemeChangeListener {
            tree.background = it.globalLaF.bgSecondary
        }
        tree.background = tm.curr.globalLaF.bgSecondary
        tree.isFocusable = false
    }

    /**
     * Builds the file tree structure recursively.
     * @param file The current file or directory.
     * @param parentNode The parent node in the tree.
     */
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

    /**
     * Represents a file or directory in the tree structure.
     * @param file The file or directory.
     * @param displayPath Whether to display the full path or just the name.
     */
    data class TreeFile(val file: File, val displayPath: Boolean = false) {
        override fun toString(): String {
            return if (displayPath) file.path else file.name
        }
    }

}

