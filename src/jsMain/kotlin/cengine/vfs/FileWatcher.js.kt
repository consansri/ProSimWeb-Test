package cengine.vfs

/**
 * JavaScript-specific implementation of [FileWatcher].
 *
 * Does nothing!
 *
 * Watching the browser localStorage is not needed.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FileWatcher actual constructor(actual val vfs: VFileSystem) {
    actual fun watchDirectory(path: String) {
        // Watching not implemented for JS
    }

    actual fun startWatching() {
        // Watching not implemented for JS
    }

    actual fun stopWatching() {
        // Watching not implemented for JS
    }

}