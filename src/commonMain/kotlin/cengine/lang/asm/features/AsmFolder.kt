package cengine.lang.asm.features

import cengine.editor.folding.CodeFoldingProvider
import cengine.editor.folding.FoldRegion
import cengine.editor.folding.FoldRegionImpl
import cengine.editor.text.Informational
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmFolder : CodeFoldingProvider {
    override val cachedFoldRegions: MutableMap<PsiFile, List<FoldRegion>> = mutableMapOf()
    override fun updateFoldRegions(psiFile: PsiFile, informational: Informational) {
        val builder = FoldRegionBuilder(psiFile, informational)
        psiFile.accept(builder)
        cachedFoldRegions.remove(psiFile)
        cachedFoldRegions[psiFile] = builder.regions
    }

    inner class FoldRegionBuilder(val psiFile: PsiFile, val informational: Informational) : PsiElementVisitor {
        val regions = mutableListOf<FoldRegionImpl>()

        override fun visitFile(file: PsiFile) {
            if (file is AsmFile) {
                file.children.forEach {
                    it.accept(this)
                }
            }
        }

        override fun visitElement(element: PsiElement) {
            when (element) {
                is ASNode.Comment -> {
                    if (element.token.type == AsmTokenType.COMMENT_ML) {
                        val firstLine = informational.getLineAndColumn(element.range.first).first
                        val lastLine = informational.getLineAndColumn(element.range.last).first
                        if (firstLine != lastLine) regions.add(FoldRegionImpl(firstLine, lastLine, false, "...*/"))
                    }
                }

                is ASNode.Program -> {
                    element.children.forEach {
                        it.accept(this)
                    }
                }
            }
        }
    }

}