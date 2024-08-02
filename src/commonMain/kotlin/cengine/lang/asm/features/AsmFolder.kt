package cengine.lang.asm.features

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegionImpl
import cengine.editor.text.Informational
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmFolder : CodeFoldingProvider {
    override var cachedFoldRegions: List<FoldRegionImpl> = listOf()
    override fun getFoldingRegions(psiFile: PsiFile, informational: Informational): List<FoldRegionImpl> {
        val builder = FoldRegionBuilder(psiFile, informational)
        psiFile.accept(builder)
        cachedFoldRegions = builder.regions
        return builder.regions
    }

    inner class FoldRegionBuilder(val psiFile: PsiFile, val informational: Informational) : PsiElementVisitor {
        val regions = mutableListOf<FoldRegionImpl>()

        override fun visitFile(file: PsiFile) {}

        override fun visitElement(element: PsiElement) {
            when (element) {
                is GASNode.Statement -> {
                    val (startIndex, endIndex) = element.textRange
                    val firstLine = informational.getLineAndColumn(startIndex.index).first
                    val lastLine = informational.getLineAndColumn(endIndex.index - 1).first
                    if (firstLine != lastLine) {
                        regions.add(FoldRegionImpl(firstLine, lastLine, false, "[...]"))
                    }
                }
            }
        }
    }

}