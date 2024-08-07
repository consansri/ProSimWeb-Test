package cengine.psi.core

import cengine.editor.text.TextModel
import cengine.lang.LanguageService
import cengine.vfs.VirtualFile

/**
 * Represents a file in the PSI structure
 */
interface PsiFile : PsiElement {
    val lang: LanguageService?
    val file: VirtualFile
    var textModel: TextModel?

    override val pathName: String
        get() = file.name

    val content: String
        get() = file.getAsUTF8String()

    fun update()
}