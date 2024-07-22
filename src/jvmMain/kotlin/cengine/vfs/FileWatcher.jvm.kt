package cengine.vfs

import debug.DebugTools
import emulator.kit.nativeLog
import java.nio.file.*
import kotlin.concurrent.thread

/**
 * JVM-specific implementation of [FileWatcher].
 *
 * This implementation uses [java.nio.file.WatchService] for file system monitoring.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
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
                            val fullPath = dir.resolve(fileName).normalize().toString().replace("\\", VFileSystem.DELIMITER).removePrefix(vfs.root.name)
                            when (kind) {
                                StandardWatchEventKinds.ENTRY_CREATE -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$fullPath-CREATED")
                                    vfs.findFile(fullPath)?.let {
                                        vfs.notifyFileCreated(it)
                                    }
                                }

                                StandardWatchEventKinds.ENTRY_DELETE -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$fullPath-DELETED")
                                    vfs.deleteFile(fullPath)
                                    vfs.findFile(fullPath)?.let {
                                        vfs.notifyFileDeleted(it)
                                    }
                                }

                                StandardWatchEventKinds.ENTRY_MODIFY -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$fullPath-MODIFIED")
                                    vfs.findFile(fullPath)?.let {
                                        vfs.notifyFileChanged(it)
                                    }
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