package cengine.psi

import cengine.editor.text.TextModel
import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import kotlinx.coroutines.*

class PsiManager<T : LanguageService>(
    private val vfs: VFileSystem,
    val lang: T
) {
    private var job: Job? = null
    private val psiCache = mutableMapOf<String, PsiFile>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()

    init {
        vfs.addChangeListener(listener)
    }

    fun updatePsi(file: VirtualFile, textModel: TextModel?, onfinish: () -> Unit = {}) {
        job?.cancel()
        job = coroutineScope.launch {
            nativeLog("Update PSI for ${file.name}")
            val psiFile = psiCache[file.path] ?: createPsiFile(file, textModel)
            psiFile.textModel = textModel
            psiFile.update()
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile
            nativeLog("Update Analytics for ${file.name}")
            lang.updateAnalytics(psiFile, textModel)
            nativeLog("Finished updating PSI!")
            onfinish()
        }
    }

    private fun createPsi(file: VirtualFile, textModel: TextModel?, onfinish: () -> Unit = {}) {
        job?.cancel()
        job = coroutineScope.launch {
            val psiFile = createPsiFile(file, textModel)
            psiFile.textModel = textModel
            psiCache[file.path] = psiFile
            lang.updateAnalytics(psiFile, null)
            onfinish()
        }
    }

    private fun removePsi(file: VirtualFile) {
        psiCache.remove(file.path)
    }

    private suspend fun createPsiFile(file: VirtualFile, textModel: TextModel?): PsiFile {
        return withContext(Dispatchers.Default) {
            lang.psiParser.parseFile(file, textModel)
        }
    }

    fun getPsiFile(file: VirtualFile): PsiFile? {
        return psiCache[file.path]
    }

    inner class VFSListener : FileChangeListener {
        override fun onFileChanged(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) updatePsi(file, null)
        }

        override fun onFileCreated(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) createPsi(file, null)
        }

        override fun onFileDeleted(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) removePsi(file)
        }
    }
}