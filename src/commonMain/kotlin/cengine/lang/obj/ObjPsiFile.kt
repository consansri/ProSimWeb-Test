package cengine.lang.obj

import cengine.editor.annotation.Annotation
import cengine.lang.LanguageService
import cengine.psi.impl.PsiFileImpl
import cengine.vfs.VirtualFile

class ObjPsiFile(file: VirtualFile, override val lang: LanguageService): PsiFileImpl(file) {
    override val annotations: List<Annotation> = emptyList()
    override val additionalInfo: String = ""
    override var range: IntRange = IntRange.EMPTY
    override fun update() {

    }
}