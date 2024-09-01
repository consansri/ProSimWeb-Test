package cengine.lang

import cengine.project.Project
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile

sealed interface RunConfiguration<T : LanguageService> {
    val name: String

    interface FileRun<T : LanguageService> : RunConfiguration<T> {
        fun run(file: VirtualFile, lang: T, vfs: VFileSystem)
    }

    interface ProjectRun<T : LanguageService> : RunConfiguration<T> {
        fun run(project: Project)
    }
}