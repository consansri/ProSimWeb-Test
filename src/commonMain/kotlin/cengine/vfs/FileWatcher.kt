package cengine.vfs

/**
 * Target specific implementation of FileWatcher which deletes/creates Files in the [vfs] or notifies the [vfs] when changes where recognized in the [watchDirectory].
 */
expect class FileWatcher(vfs: VFileSystem) {
    val vfs: VFileSystem

    /**
     * Stats watching a directory for changes.
     *
     * @param path The path of the directory to watch.
     */
    fun watchDirectory(path: String)

    /**
     * Starts the file-watching process.
     */
    fun startWatching()

    /**
     * Stops the file-watching process.
     */
    fun stopWatching()
}