package cengine.project

import cengine.editor.CodeEditor
import cengine.lang.LanguageService
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.TargetSpec
import cengine.psi.PsiManager
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile

/**
 * @property services [AsmLang] gets always added by the [Project] with specification from [ProjectState].
 */
class Project(initialState: ProjectState, vararg languageServices: LanguageService) : FileChangeListener {
    val projectState: ProjectState = initialState
    val services: Set<LanguageService> = languageServices.toSet() + AsmLang(TargetSpec.specs.firstOrNull { it.name == initialState.target   } ?: TargetSpec.specs.first())
    val fileSystem: VFileSystem = VFileSystem(projectState.absRootPath)
    val psiManagers: List<PsiManager<*>> = services.map { PsiManager(fileSystem, it) }
    val currentEditors: MutableList<CodeEditor> = mutableListOf()

    init {
        fileSystem.addChangeListener(this)
    }

    fun getManager(lang: LanguageService): PsiManager<*>? = psiManagers.firstOrNull { lang::class == it.lang::class }
    fun getLang(file: VirtualFile): LanguageService? = getManager(file)?.lang

    fun getManager(file: VirtualFile): PsiManager<*>? {
        val service = services.firstOrNull { file.name.endsWith(it.fileSuffix) } ?: return null
        return getManager(service)
    }

    fun register(editor: CodeEditor) {
        currentEditors.add(editor)
    }

    fun unregister(editor: CodeEditor) {
        currentEditors.remove(editor)
    }

    override fun onFileChanged(file: VirtualFile) {
        currentEditors.firstOrNull { it.file == file }?.loadFromFile()
    }

    override fun onFileCreated(file: VirtualFile) {
        // nothing
    }

    override fun onFileDeleted(file: VirtualFile) {
        // nothing
    }

    fun close() {
        fileSystem.close()
        currentEditors.clear()
    }
}