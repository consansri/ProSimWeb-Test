package cengine.lang.asm

import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import emulator.kit.assembler.AsmFile
import emulator.kit.assembler.Assembler
import emulator.kit.assembler.Process

class AsmPsiParser(val assembler: Assembler, val languageService: AsmLang) : PsiParser {
    override fun parseFile(content: String, fileName: String): PsiFile {
        val result = assembler.compile(AsmFile(fileName, fileName, content), listOf(), Process.Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD)
        val psiFile = cengine.lang.asm.psi.AsmFile(fileName, content, languageService)
        return psiFile
    }
}