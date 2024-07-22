package cengine.vfs.tree

import cengine.vfs.VirtualFile

/**
 * Listener interface for file tree change events.
 */
interface FileTreeUIChangeListener {
    fun onNodeSelected(file: VirtualFile)
    fun onNodeExpanded(directory: VirtualFile)
    fun onNodeCollapsed(directory: VirtualFile)
    fun onCreateRequest(parentDirectory: VirtualFile, name: String, isDirectory: Boolean)
    fun onDeleteRequest(file: VirtualFile)
    fun onRenameRequest(file: VirtualFile, newName: String)
    fun onOpenRequest(file: VirtualFile)
}