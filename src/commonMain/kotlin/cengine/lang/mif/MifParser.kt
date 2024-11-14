package cengine.lang.mif

import cengine.lang.mif.ast.MifLexer
import cengine.lang.mif.ast.MifNode
import cengine.lang.mif.ast.MifPsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

object MifParser: PsiParser<MifPsiFile> {
    override fun parse(file: VirtualFile): MifPsiFile {
        val content = file.getAsUTF8String()
        val lexer = MifLexer(content)
        val program = MifNode.Program.parse(lexer)

        program.accept(PsiParser.ParentLinker())

        return MifPsiFile(file, program)
    }
}