package cengine.lang.asm

import cengine.psi.core.LanguageService
import cengine.psi.core.PsiElement
import cengine.psi.impl.PsiFileImpl

class AsmFileImpl(name: String, text: String): PsiFileImpl(name, text) {

    fun addChild(child: PsiElement){
        (children as MutableList<PsiElement>).add(child)
        (child as AsmElementImpl).parent = this
    }

    override val lang: LanguageService?
        get() = TODO("Not yet implemented")

    override fun updateFrom(content: String) {
        TODO("Not yet implemented")
    }

}