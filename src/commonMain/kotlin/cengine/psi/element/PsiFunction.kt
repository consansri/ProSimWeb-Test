package cengine.psi.element

import cengine.psi.core.PsiElement

interface PsiFunction: PsiElement {
    val name: String
    val parameters: List<PsiParameter>
    val body: PsiBlock
}