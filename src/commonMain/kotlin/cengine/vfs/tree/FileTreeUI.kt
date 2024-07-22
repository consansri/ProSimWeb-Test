package cengine.vfs.tree

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
    fun expandNode(path: String)

    /**
     * Collapse a directory node in the tree.
     *
     * @param path The path of the directory to collapse.
     */
    fun collapseNode(path: String)

    /**
     * Select a node in the tree.
     *
     * @param path The path of the node to select.
     */
    fun selectNode(path: String)

    /**
     * Create a new file or directory.
     *
     * @param parentPath The path of the parent directory.
     * @param name The name of the new file or directory.
     * @param isDirectory True, if creating a directory, false for a file.
     */
    fun createNode(parentPath: String, name: String, isDirectory: Boolean)

    /**
     * Delete a file or directory through the UI.
     *
     * @param path the Path of the file or directory to delete.
     */
    fun deleteNode(path: String)

    /**
     * Rename a file or directory through the UI.
     *
     * @param path The current path of the file or directory.
     * @param newName The new name for the file or directory.
     */
    fun renameNode(path: String, newName: String)

    /**
     * Open a file in an editor.
     *
     * @param path The path of the file to open.
     */
    fun openFile(path: String)

    /**
     * Set a listener for file tree events.
     *
     * @param listener The [FileTreeUIChangeListener] to set.
     */
    fun setFileTreeListener(listener: FileTreeUIChangeListener)

}