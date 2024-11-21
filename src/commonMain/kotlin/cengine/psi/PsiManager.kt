package cengine.psi

import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.FPath
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.coroutines.*

class PsiManager<L : LanguageService, F: PsiFile>(
    private val vfs: VFileSystem,
    val lang: L,
    val psiParser: PsiParser<F>
) {
    private var job: Job? = null
    private val psiCache = mutableMapOf<FPath, F>()
    private val psiUpdateScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()

    init {
        vfs.addChangeListener(listener)
    }

    fun queueUpdate(file: VirtualFile, onfinish: suspend (F) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            delay(1000L)
            onfinish(updatePsi(file))
        }
    }

    suspend fun updatePsi(file: VirtualFile): F {
        val psiFile = psiCache[file.path]
        return if (psiFile == null) {
            val created = createPsiFile(file)
            lang.updateAnalytics(created)
            created
        } else {
            psiFile.update()
            lang.updateAnalytics(psiFile)
            psiFile
        }
    }

    fun inserted(file: VirtualFile, index: Int, length: Int): F? {
        val psiFile = getPsiFile(file) ?: return null

        psiFile.inserted(index, length)

        return psiFile
    }

    fun deleted(file: VirtualFile, start: Int, end: Int): F? {
        val psiFile = getPsiFile(file) ?: return null

        psiFile.deleted(start, end)

        return psiFile
    }

    fun queueInsertion(file: VirtualFile, index: Int, length: Int, onfinish: suspend (F) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(inserted(file, index, length) ?: run {
                createPsiFile(file)
            })
        }
    }

    fun queueDeletion(file: VirtualFile, start: Int, end: Int, onfinish: suspend (F) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(deleted(file, start, end) ?: run {
                createPsiFile(file)
            })
        }
    }

    private fun createPsi(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            val psiFile = createPsiFile(file)
            lang.updateAnalytics(psiFile)
            onfinish(psiFile)
        }
    }

    private fun replacePsi(psiFile: F){
        psiCache.remove(psiFile.file.path)
        psiCache[psiFile.file.path] = psiFile
    }

    private fun removePsi(file: VirtualFile) {
        psiCache.remove(file.path)
    }

    private suspend fun createPsiFile(file: VirtualFile): F {
        return withContext(Dispatchers.Default) {
            val psiFile = psiParser.parse(file)
            replacePsi(psiFile)
            psiFile
        }
    }

    fun getPsiFile(file: VirtualFile): F? {
        return psiCache[file.path]
    }

    fun printCache(): String = psiCache.toString()

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