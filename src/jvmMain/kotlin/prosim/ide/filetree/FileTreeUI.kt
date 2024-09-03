package prosim.ide.filetree

import cengine.vfs.FPath

interface FileTreeUI {

    /**
     * Refresh the file tree display.
     */
    fun refresh()

    /**
     * Expand a directory node in the tree.
     *
     * @param path The path of the directory to expand.
     */
    fun expandNode(path: FPath)

    /**
     * Collapse a directory node in the tree.
     *
     * @param path The path of the directory to collapse.
     */
    fun collapseNode(path: FPath)

    /**
     * Select a node in the tree.
     *
     * @param path The path of the node to select.
     */
    fun selectNode(path: FPath)

    /**
     * Create a new file or directory.
     *
     * @param parentPath The path of the parent directory.
     * @param name The name of the new file or directory.
     * @param isDirectory True, if creating a directory, false for a file.
     */
    fun createNode(parentPath: FPath, name: String, isDirectory: Boolean)

    /**
     * Delete a file or directory through the UI.
     *
     * @param path the Path of the file or directory to delete.
     */
    fun deleteNode(path: FPath)

    /**
     * Rename a file or directory through the UI.
     *
     * @param path The current path of the file or directory.
     * @param newName The new name for the file or directory.
     */
    fun renameNode(path: FPath, newName: String)

    /**
     * Open a file in an editor.
     *
     * @param path The path of the file to open.
     */
    fun openFile(path: FPath)

    /**
     * Set a listener for file tree events.
     *
     * @param listener The [FileTreeUIListener] to set.
     */
    fun setFileTreeListener(listener: FileTreeUIListener)

}