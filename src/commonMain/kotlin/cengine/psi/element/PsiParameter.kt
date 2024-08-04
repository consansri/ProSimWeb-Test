package cengine.psi.element

import cengine.psi.core.PsiElement

interface PsiParameter: PsiElement {
    val name: String
    val type: PsiType

    override val pathName: String
        get() = name + type
}