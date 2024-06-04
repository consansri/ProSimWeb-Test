package me.c3.ui.workspace

import emulator.kit.assembler.AsmFile
import emulator.kit.common.FileBuilder
import emulator.kit.common.FileBuilder.ExportFormat
import emulator.kit.toAsmFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.c3.ui.Events
import me.c3.ui.Keys
import me.c3.ui.States
import me.c3.ui.States.setFromPath
import me.c3.ui.styled.*
import me.c3.ui.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Manages the workspace for the code editor, including the file tree and related UI components.
 * @param path The path to the workspace directory.
 * @param codeEditor The code editor instance.
 * @param mainManager The main manager instance.
 */
class Workspace(private val path: String, var editor: WSEditor? = null, var logger: WSLogger? = null) {
    // Root directory of the workspace.
    val rootDir = File(path)

    // Settings
    val config: WSConfig

    // Root node of the file tree.
    private val rootNode = DefaultMutableTreeNode(TreeFile(rootDir))

    // Tree model representing the file tree structure.
    private val treeModel = DefaultTreeModel(rootNode)

    // UI component representing the file tree.
    val tree: CTree

    init {
        config = WSConfig(Keys.getConfigFile(rootDir)) {
            editor?.updateFile(it)
        }

        // Build the file tree starting from the root directory.
        buildFileTree(rootDir, rootNode)

        // Initialize the file tree UI component.
        tree = CTree(treeModel, FontType.BASIC)

        // Add mouse listener for handling user interactions with the file tree.
        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {}

            override fun mouseClicked(e: MouseEvent) {
                val nodes = tree.selectionPaths?.mapNotNull { it.lastPathComponent as? DefaultMutableTreeNode } ?: return
                val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
                val uobj = selectedNode.userObject
                if (uobj !is TreeFile) return
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.x, e.y, nodes)
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (uobj.file.isFile) {
                        if (e.clickCount == 2) {
                            editor?.openFile(uobj.file)
                        }
                    }
                    logger?.log(getFileWithProjectPath(uobj.file).formatPathForDrawing())
                }
            }
        })

        // Set the initial look and feel of the file tree.
        setTreeLook()
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
     * @param main The file to exclude from the list.
     * @return A list of compiler files.
     */
    fun getImportableFiles(main: File): List<AsmFile> {
        return getAllFiles(main.parentFile).filter { it != main && it.isFile && (it.name.endsWith(".s") || it.name.endsWith(".S")) }.map { it.toAsmFile(main, rootDir) }
    }

    /**
     * Displays a context menu with various file operations.
     * @param mainManager The main manager instance.
     * @param treeFile The file for which the context menu is shown.
     * @param x The x-coordinate of the context menu location.
     * @param y The y-coordinate of the context menu location.
     */
    private fun showContextMenu(x: Int, y: Int, allSelectedNodes: Collection<DefaultMutableTreeNode>) {
        val treeFiles = allSelectedNodes.mapNotNull { it.userObject as? TreeFile }

        val popupMenu = CPopupMenu()

        val dirs = treeFiles.filter { it.file.isDirectory }
        val files = treeFiles.filter { it.file.isFile }
        val asmFiles = treeFiles.filter { it.file.isFile && (it.file.name.endsWith(".s") || it.file.endsWith(".S")) }

        val containsDirs = dirs.isNotEmpty()
        val containsFiles = files.isNotEmpty()
        val containsAsmFiles = asmFiles.isNotEmpty()

        /**
         * Initialize Menu Items
         */

        // Only Directory
        val createFileItem = if (containsDirs) CMenuItem("New File") else null
        val createDirItem = if (containsDirs) CMenuItem("New Directory") else null
        val reloadFromDisk = if (containsDirs) CMenuItem("Reload") else null

        // Only File
        val openItem = if (containsFiles) CMenuItem("Open") else null

        // Only Assembly File
        val buildFile = if (containsAsmFiles) CMenuItem("Build") else null
        val exportMIF = if (containsAsmFiles) CMenuItem("Generate MIF") else null
        val exportHexDump = if (containsAsmFiles) CMenuItem("Generate HexDump") else null
        val exportVHDL = if (containsAsmFiles) CMenuItem("Generate VHDL") else null
        val exportTS = if (containsAsmFiles) CMenuItem("Generate Transcript") else null

        // General
        val renameItem = CMenuItem("Rename")
        val deleteItem = CMenuItem("Delete")

        /**
         * Implement Menu Actions
         */

        // Directories

        createDirItem?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newDirName = COptionPane.showInputDialog(tree, "Enter directory name:").await()
                if (newDirName.isNotBlank()) {
                    dirs.forEach {
                        val newDir = File(it.file, newDirName)
                        if (newDir.mkdir()) {
                            // File created successfully
                            States.ws.setFromPath(path, editor, logger)
                        } else {
                            logger?.error("Failed to create directory $newDirName!")
                        }
                    }
                }
            }
        }

        createFileItem?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newFileName = COptionPane.showInputDialog(tree, "Enter file name:").await()
                if (newFileName.isNotBlank()) {
                    dirs.forEach {
                        val newFile = File(it.file, newFileName)
                        if (newFile.createNewFile()) {
                            // File created successfully
                            States.ws.setFromPath(path, editor, logger)
                        } else {
                            logger?.error("Failed to create file $newFileName!")
                        }
                    }
                }
            }
        }

        reloadFromDisk?.addActionListener {
            CoroutineScope(Dispatchers.IO).launch {
                rootNode.removeAllChildren()
                buildFileTree(rootDir, rootNode)
                withContext(Dispatchers.Main) {
                    treeModel.reload()
                    tree.revalidate()
                    tree.repaint()
                }
            }
        }

        // Files

        openItem?.addActionListener {
            files.forEach {
                if (it.file.isFile) {
                    editor?.openFile(it.file)
                }
            }
        }

        buildFile?.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                files.forEach {
                    val result = States.arch.get().compile(it.file.toAsmFile(it.file, rootDir), getImportableFiles(it.file), true)
                    Events.compile.triggerEvent(result)
                }
            }
        }

        exportMIF?.addActionListener {
            showExportOverlay(ExportFormat.MIF, files)
        }

        exportVHDL?.addActionListener {
            showExportOverlay(ExportFormat.VHDL, files)
        }

        exportHexDump?.addActionListener {
            showExportOverlay(ExportFormat.HEXDUMP, files)
        }

        exportTS?.addActionListener {
            showExportOverlay(ExportFormat.TRANSCRIPT, files)
        }

        // General

        renameItem.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newFileName = COptionPane.showInputDialog(tree, "Enter new file name:").await()
                if (newFileName.isNotBlank()) {
                    treeFiles.forEach {
                        val newFile = File(it.file.parentFile, newFileName)
                        if (it.file.renameTo(newFile)) {
                            // File renamed successfully
                            States.ws.setFromPath(path, editor, logger)
                        } else {
                            logger?.error("Failed to rename file $newFileName!")
                        }
                    }
                }
            }
        }

        deleteItem.addActionListener {
            CoroutineScope(Dispatchers.Main).launch {
                var filesInDirs = 0
                dirs.forEach { filesInDirs += it.file.listFiles()?.filter { child -> !files.map { it.file }.contains(child) }?.size ?: 0 }
                val fileInfoString = when {
                    dirs.isNotEmpty() && files.isNotEmpty() -> "${dirs.size} directories (with $filesInDirs files) and ${files.size} files"
                    dirs.isNotEmpty() -> "${dirs.size} directories (with $filesInDirs files)"
                    files.isNotEmpty() -> "${files.size} files"
                    else -> "nothing"
                }

                val confirmation = COptionPane.confirm(
                    tree,
                    "You are about to delete $fileInfoString"
                ).await()

                if (confirmation) {
                    files.forEach {
                        if (!it.file.delete()) {
                            logger?.error("Failed to delete file ${it.file.name}!")
                            return@launch
                        }
                    }

                    dirs.forEach {
                        if (!it.file.deleteRecursively()) {
                            logger?.error("Failed to delete directory ${it.file.name}!")
                            return@launch
                        }
                    }

                    // Files deleted successfully
                    States.ws.setFromPath(path, editor, logger)
                    logger?.log("Deleted $fileInfoString")
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

    private suspend fun exportFile(type: ExportFormat, file: File, vararg settings: FileBuilder.Setting) {
        val result = States.arch.get().compile(file.toAsmFile(file, rootDir), getImportableFiles(file), true)
        Events.compile.triggerEvent(result)
        if (!result.success) {
            return
        }
        withContext(Dispatchers.IO) {
            val fileContent = States.arch.get().getFormattedFile(type, file.toAsmFile(file, rootDir), getImportableFiles(file), *settings)
            val generatedDir = File(file.parentFile, ".generated")
            if (!generatedDir.exists()) {
                generatedDir.mkdir()
            }

            Events.compile.triggerEvent(result)

            val newFile = File(generatedDir, file.name.removeSuffix(".s") + type.ending)
            if (newFile.createNewFile()) {
                newFile.writeText(fileContent.joinToString("\n") { it })
                // File build successfully
                States.ws.setFromPath(path, editor, logger)
            } else {
                logger?.error("Failed to save generated file ${newFile.name}!")
            }
        }
    }

    private fun showExportOverlay(type: ExportFormat, files: List<TreeFile>) {
        SwingUtilities.invokeLater {
            val (dialog, content, submit) = CDialog.createWithTitle("Export - ${type.uiName}", tree) {
                // onClose
            }

            content.layout = GridBagLayout()
            val contentGBC = GridBagConstraints()
            contentGBC.weightx = 1.0
            contentGBC.insets = States.scale.get().borderScale.getInsets()
            contentGBC.fill = GridBagConstraints.HORIZONTAL

            submit.layout = GridBagLayout()
            val submitGBC = GridBagConstraints()
            submitGBC.weightx = 1.0
            submitGBC.fill = GridBagConstraints.HORIZONTAL

            when (type) {
                ExportFormat.VHDL, ExportFormat.MIF, ExportFormat.HEXDUMP -> {
                    val addrLabel = CLabel("Address Width [Bits]", FontType.BASIC)
                    val wordLabel = CLabel("Data Width [Bits]", FontType.BASIC)
                    val addrSpinner = CSpinner(SpinnerNumberModel(States.arch.get().memory.addressSize.bitWidth, 8, Int.MAX_VALUE, 8))
                    val wordSpinner = CSpinner(SpinnerNumberModel(States.arch.get().memory.instanceSize.bitWidth, 8, Int.MAX_VALUE, 8))
                    val export = CTextButton("Export", FontType.CODE).apply {
                        addActionListener {
                            CoroutineScope(Dispatchers.Default).launch {
                                files.forEach {
                                    exportFile(type, it.file, FileBuilder.Setting.AddressWidth(addrSpinner.value as Int), FileBuilder.Setting.DataWidth(wordSpinner.value as Int))
                                }
                            }
                            dialog.dispose()
                        }
                    }

                    contentGBC.gridy = 0
                    content.add(addrLabel, contentGBC)
                    contentGBC.gridy = 1
                    content.add(wordLabel, contentGBC)

                    contentGBC.gridx = 1
                    contentGBC.gridy = 0
                    content.add(addrSpinner, contentGBC)
                    contentGBC.gridy = 1
                    content.add(wordSpinner, contentGBC)

                    submit.add(export, submitGBC)
                }

                ExportFormat.CURRENT_FILE, ExportFormat.TRANSCRIPT -> {
                    val export = CTextButton("Export", FontType.CODE).apply {
                        addActionListener {
                            CoroutineScope(Dispatchers.Default).launch {
                                files.forEach {
                                    exportFile(type, it.file)
                                }
                            }
                            dialog.dispose()
                        }
                    }
                    submit.add(export, submitGBC)
                }
            }

            dialog.isVisible = true
        }
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
    private fun setTreeLook() {
        States.theme.addEvent { theme ->
            tree.background = theme.globalLaF.bgSecondary
        }
        tree.background = States.theme.get().globalLaF.bgSecondary
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

