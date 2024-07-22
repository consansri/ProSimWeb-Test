package cengine.lang.asm

import cengine.lang.asm.psi.dir.AsmDir
import cengine.lang.asm.psi.instr.AsmInstr
import cengine.lang.asm.psi.stmnt.*
import cengine.psi.core.PsiParser
import cengine.psi.core.TextRange
import emulator.kit.assembler.AsmFile
import emulator.kit.assembler.Assembler
import emulator.kit.assembler.Process
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Token

class AsmPsiParser(val assembler: Assembler, val languageService: AsmLang) : PsiParser {
    override fun parseFile(content: String, fileName: String): cengine.lang.asm.psi.AsmFile {
        //nativeLog("AsmPsiParser parses file: $fileName!")
        val result = assembler.compile(AsmFile(fileName, fileName, content), listOf(), Process.Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD)
        //nativeLog(result.tree.toString())
        val psiFile = cengine.lang.asm.psi.AsmFile(fileName, content, languageService)
        result.tree.rootNode?.getAllStatements()?.forEach { stmnt ->
            psiFile.children.add(stmnt.parse())
        }

        return psiFile
    }

    private fun GASNode.Statement.parse(): AsmStatement {
        val lineLoc = getLineLoc()
        return when (this) {
            is GASNode.Statement.Dir -> AsmDirStmnt(label?.parse(), dir.parse(), lineLoc.toTextRange())
            is GASNode.Statement.Empty -> AsmEmptyStmnt(label?.parse(), lineLoc.toTextRange())
            is GASNode.Statement.Instr -> AsmInstrStmnt(label?.parse(), rawInstr.parse(), lineLoc.toTextRange())
            is GASNode.Statement.Unresolved -> AsmEmptyStmnt(null, lineLoc.toTextRange())
        }
    }

    private fun GASNode.Label.parse(): AsmLabel {
        val lineLoc = getLineLoc()
        return AsmLabel(null, this.identifier, lineLoc.toTextRange())
    }

    private fun GASNode.Directive.parse(): AsmDir {
        val lineLoc = getLineLoc()
        return AsmDir(null, listOf(), lineLoc.toTextRange())
    }

    private fun GASNode.RawInstr.parse(): AsmInstr {
        val lineLoc = getLineLoc()
        return AsmInstr(null, listOf(), lineLoc.toTextRange())
    }

    private fun Token.LineLoc.toTextRange(): TextRange = TextRange(this.startIndex, this.endIndex)

}