package cengine.psi.core

import cengine.vfs.VirtualFile

/**
 * Service for managing PSI-related operations
 */
interface PsiService {
    fun createFile(file: VirtualFile): PsiFile
    fun findElementAt(file: PsiFile, offset: Int): PsiElement?
    fun findReferences(element: PsiElement): List<PsiReference>
}