package cengine.vfs

import java.nio.file.*
import kotlin.concurrent.thread

/**
 * JVM-specific implementation of [FileWatcher].
 *
 * This implementation uses [java.nio.file.WatchService] for file system monitoring.
 */
actual class FileWatcher actual constructor(actual val vfs: VFileSystem) {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchKeys = mutableMapOf<WatchKey, Path>()
    private var watchThread: Thread? = null
    private var isWatching = false

    actual fun watchDirectory(path: String) {
        val dir = Paths.get(path)
        val watchKey = dir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
        watchKeys[watchKey] = dir
    }

    actual fun startWatching() {
        if (isWatching) return
        isWatching = true
        watchThread = thread(start = true) {
            while (isWatching) {
                val key = watchService.take()
                val dir = watchKeys[key]
                if (dir != null) {
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        val fileName = event.context() as? Path
                        if (fileName != null) {
                            val fullPath = dir.resolve(fileName).toString()
                            when (kind) {
                                StandardWatchEventKinds.ENTRY_CREATE -> vfs.createFile(fullPath)
                                StandardWatchEventKinds.ENTRY_DELETE -> vfs.deleteFile(fullPath)
                                StandardWatchEventKinds.ENTRY_MODIFY -> vfs.findFile(fullPath)?.let {
                                    vfs.notifyFileChanged(it)
                                }
                            }
                        }
                    }
                }
                key.reset()
            }
        }
    }

    actual fun stopWatching() {
        isWatching = false
        watchThread?.interrupt()
        watchThread = null
        watchService.close()
    }

}