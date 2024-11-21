package cengine.lang

import cengine.project.Project
import cengine.vfs.VirtualFile

abstract class Runner<T : LanguageService>(val lang: T, val name: String) {

    abstract suspend fun global(project: Project, vararg attrs: String)

    abstract suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String)
}