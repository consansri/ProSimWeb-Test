package cengine.psi.core

import cengine.editor.text.TextModel
import cengine.lang.LanguageService
import cengine.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope

/**
 * Represents a file in the PSI structure
 */
interface PsiFile : PsiElement {
    val quickeditScope: CoroutineScope

    val lang: LanguageService?
    val file: VirtualFile
    var textModel: TextModel?

    override val pathName: String
        get() = file.name

    override suspend fun deleted(start: Int, end: Int) {
        super.deleted(start, end)
        textRange = textRange.shrink(end - start)
    }

    override suspend fun inserted(index: Int, value: String) {
        super.inserted(index, value)
        textRange = textRange.expand(value.length)
    }
    val content: String
        get() = file.getAsUTF8String()

    fun update()
}