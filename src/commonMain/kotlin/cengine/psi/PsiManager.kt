package cengine.psi

import cengine.editor.text.TextModel
import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.coroutines.*

class PsiManager<T : LanguageService>(
    private val vfs: VFileSystem,
    val lang: T
) {
    private var job: Job? = null
    private val psiCache = mutableMapOf<String, PsiFile>()
    private val psiUpdateScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()

    init {
        vfs.addChangeListener(listener)
    }

    fun queueUpdate(file: VirtualFile, textModel: TextModel?, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            delay(500L)
            updatePsi(file, textModel, onfinish)
        }
    }

    suspend fun updatePsi(file: VirtualFile, textModel: TextModel?, onfinish: suspend (PsiFile) -> Unit = {}) {
        val psiFile = psiCache[file.path]
        if (psiFile == null) {
            val created = createPsiFile(file, textModel)
            created.textModel = textModel
            psiCache[file.path] = created
            lang.updateAnalytics(created, textModel)
            onfinish(created)
        } else {
            psiFile.textModel = textModel
            psiFile.update()
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile
            lang.updateAnalytics(psiFile, textModel)
            onfinish(psiFile)
        }
    }

    fun inserted(file: VirtualFile, textModel: TextModel?, index: Int, value: String, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file, textModel, onfinish)
        psiUpdateScope.launch {
            val psiFile = psiCache[file.path] ?: run {
                createPsiFile(file, textModel)
            }
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile

            psiFile.inserted(index, value)

            onfinish(psiFile)
        }
    }

    fun deleted(file: VirtualFile, textModel: TextModel?, start: Int, end: Int, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file, textModel, onfinish)
        psiUpdateScope.launch {
            val psiFile = psiCache[file.path] ?: run {
                createPsiFile(file, textModel)
            }
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile

            psiFile.deleted(start, end)

            onfinish(psiFile)
        }
    }

    private fun createPsi(file: VirtualFile, textModel: TextModel?, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            val psiFile = createPsiFile(file, textModel)
            psiFile.textModel = textModel
            psiCache[file.path] = psiFile
            lang.updateAnalytics(psiFile, null)
            onfinish(psiFile)
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
            if (file.name.endsWith(lang.fileSuffix)) queueUpdate(file, null)
        }

        override fun onFileCreated(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) createPsi(file, null)
        }

        override fun onFileDeleted(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) removePsi(file)
        }
    }
}