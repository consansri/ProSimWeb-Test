package prosim.ide.filetree

import cengine.vfs.VirtualFile

open class FileTreeUIAdapter : FileTreeUIListener {
    override fun onNodeSelected(file: VirtualFile) {}

    override fun onNodeExpanded(directory: VirtualFile) {}

    override fun onNodeCollapsed(directory: VirtualFile) {}

    override fun onCreateRequest(parentDirectory: VirtualFile, name: String, isDirectory: Boolean) {}

    override fun onDeleteRequest(file: VirtualFile) {}

    override fun onRenameRequest(file: VirtualFile, newName: String) {}

    override fun onOpenRequest(file: VirtualFile) {}
}