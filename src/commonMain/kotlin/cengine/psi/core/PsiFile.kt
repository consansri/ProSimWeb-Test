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

    val name: String
        get() = file.name

    val content: String
        get() = file.getAsUTF8String()

    fun update()
}