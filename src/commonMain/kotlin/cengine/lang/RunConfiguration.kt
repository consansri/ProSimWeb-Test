package cengine.lang

import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile

sealed interface RunConfiguration {
    val name: String
    interface FileRun : RunConfiguration {
        fun run(file: VirtualFile, vfs: VFileSystem)
    }
}