package cengine.psi

import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.vfs.FPath
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.coroutines.*

class PsiManager<T : LanguageService>(
    private val vfs: VFileSystem,
    val lang: T
) {
    private var job: Job? = null
    private val psiCache = mutableMapOf<FPath, PsiFile>()
    private val psiUpdateScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()

    init {
        vfs.addChangeListener(listener)
    }

    fun queueUpdate(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            delay(1000L)
            updatePsi(file, onfinish)
        }
    }

    suspend fun updatePsi(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        val psiFile = psiCache[file.path]
        if (psiFile == null) {
            val created = createPsiFile(file)
            psiCache[file.path] = created
            lang.updateAnalytics(created)
            onfinish(created)
        } else {
            psiFile.update()
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile
            lang.updateAnalytics(psiFile)
            onfinish(psiFile)
        }
    }

    fun inserted(file: VirtualFile, index: Int, value: String, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            val psiFile = psiCache[file.path] ?: run {
                createPsiFile(file)
            }
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile

            psiFile.inserted(index, value)

            onfinish(psiFile)
        }
    }

    fun deleted(file: VirtualFile, start: Int, end: Int, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file,  onfinish)
        psiUpdateScope.launch {
            val psiFile = psiCache[file.path] ?: run {
                createPsiFile(file)
            }
            psiCache.remove(file.path)
            psiCache[file.path] = psiFile

            psiFile.deleted(start, end)

            onfinish(psiFile)
        }
    }

    private fun createPsi(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            val psiFile = createPsiFile(file)
            psiCache[file.path] = psiFile
            lang.updateAnalytics(psiFile)
            onfinish(psiFile)
        }
    }

    private fun removePsi(file: VirtualFile) {
        psiCache.remove(file.path)
    }

    private suspend fun createPsiFile(file: VirtualFile): PsiFile {
        return withContext(Dispatchers.Default) {
            lang.psiParser.parse(file)
        }
    }

    fun getPsiFile(file: VirtualFile): PsiFile? {
        return psiCache[file.path]
    }

    inner class VFSListener : FileChangeListener {
        override fun onFileChanged(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) queueUpdate(file)
        }

        override fun onFileCreated(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) createPsi(file)
        }

        override fun onFileDeleted(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) removePsi(file)
        }
    }
}