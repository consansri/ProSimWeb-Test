package cengine.psi

import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PsiManager<T: LanguageService>(
    private val vfs: VFileSystem,
    val lang: T
) {
    private val psiCache = mutableMapOf<String, PsiFile>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()
    init {
        vfs.addChangeListener(listener)
    }

    fun updatePsi(file: VirtualFile) {
        coroutineScope.launch {
            val psiFile = psiCache[file.path] ?: createPsiFile(file)
            val newContent = file.getAsUTF8String()
            psiFile.updateFrom(newContent)
        }
    }

    private fun createPsi(file: VirtualFile) {
        coroutineScope.launch {
            val psiFile = createPsiFile(file)
            psiCache[file.path] = psiFile
        }
    }

    private fun removePsi(file: VirtualFile) {
        psiCache.remove(file.path)
    }

    private suspend fun createPsiFile(file: VirtualFile): PsiFile {
        val content = file.getAsUTF8String()
        return withContext(Dispatchers.Default) {
            lang.psiParser.parseFile(content, file.path)
        }
    }

    fun getPsiFile(file: VirtualFile): PsiFile? {
        return psiCache[file.path]
    }

    inner class VFSListener: FileChangeListener{
        override fun onFileChanged(file: VirtualFile) {
            updatePsi(file)
        }

        override fun onFileCreated(file: VirtualFile) {
            createPsi(file)
        }

        override fun onFileDeleted(file: VirtualFile) {
            removePsi(file)
        }
    }
}