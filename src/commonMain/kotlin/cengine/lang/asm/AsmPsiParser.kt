package cengine.lang.asm

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.ast.gas.GASNodeType
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog

class AsmPsiParser(val asmSpec: AsmSpec, val languageService: AsmLang) : PsiParser {

    override fun parseFile(file: VirtualFile, textModel: TextModel?): AsmFile {
        nativeLog("Parsing file ...")

        val content = try {
            textModel?.toString() ?: file.getAsUTF8String()
        } catch (e: ConcurrentModificationException) {
            textModel?.toString() ?: file.getAsUTF8String()
        }

        val lexer = asmSpec.createLexer(content)
        lexer.reset(content)

        val program = GASNode.buildNode(GASNodeType.PROGRAM, lexer, asmSpec) as GASNode.Program

        program.accept(ParentLinker())
        program.notations.addAll(lexer.error.map { Notation.error(it, "Unexpected char!") })
        program.notations.addAll(lexer.ignored.map { Notation.info(it, "Is ignored!") })

        nativeLog("Parsed File!")

        //nativeLog("AsmPsiParser parses file: $fileName!")

        return AsmFile(file, languageService, program).apply {
            this.textModel = textModel
        }
    }

    fun reparseStatements(fromIndex: Int, toIndex: Int, asmFile: AsmFile): List<GASNode.Statement> {
        val lexer = asmSpec.createLexer("")
        val content = asmFile.file.getAsUTF8String().substring(fromIndex, toIndex)
        lexer.reset(content)
        val program = GASNode.buildNode(GASNodeType.PROGRAM, lexer, asmSpec) as GASNode.Program
        return program.getAllStatements()
    }

    private inner class ParentLinker : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach { it.parent = file }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is GASNode) return
            element.children.forEach { it.parent = element }
        }
    }

    private inner class MacroResolution: PsiElementVisitor {

        override fun visitFile(file: PsiFile) {
            TODO("Not yet implemented")
        }

        override fun visitElement(element: PsiElement) {
            TODO("Not yet implemented")
        }
    }

}