package cengine.lang.asm.features

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegionImpl
import cengine.editor.text.Informational
import cengine.lang.asm.psi.AsmFile
import cengine.lang.asm.psi.stmnt.AsmStatement
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmFolder(private val informational: Informational) : CodeFoldingProvider {
    override var cachedFoldRegions: List<FoldRegionImpl> = listOf()

    override fun getFoldingRegions(psiFile: PsiFile): List<FoldRegionImpl> {
        if (psiFile !is AsmFile) return emptyList()

        return emptyList()
    }

    inner class FoldRegionBuilder : PsiElementVisitor {
        val regions = mutableListOf<FoldRegionImpl>()

        override fun visitFile(file: PsiFile) {
            file.accept(this)
        }

        override fun visitElement(element: PsiElement) {
            when (element) {
                is AsmStatement -> {
                    val (startIndex, endIndex) = element.textRange
                    val (startLine) = informational.getLineAndColumn(startIndex)
                    val (endLine) = informational.getLineAndColumn(endIndex)
                    regions.add(FoldRegionImpl(startLine, endLine, false, "[...]"))
                }
            }
        }
    }

}