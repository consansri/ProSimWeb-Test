package cengine.lang.asm

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.ast.gas.GASNodeType
import cengine.lang.asm.psi.AsmFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog

class AsmPsiParser(val asmSpec: AsmSpec, val languageService: AsmLang) : PsiParser {



    override fun parseFile(file: VirtualFile): AsmFile {
        nativeLog("Parsing file ...")
        val lexer = asmSpec.createLexer("")
        val content = file.getAsUTF8String()
        lexer.reset(content)
     /*   val initialPos = lexer.position
        val tokens = mutableListOf<AsmToken>()

        while (lexer.hasMoreTokens()) {
            tokens.add(lexer.consume(true))
        }*/
/*
        nativeLog("Tokens: ${tokens.joinToString { it.toString() }}")

        lexer.position = initialPos*/
        val program = GASNode.buildNode(GASNodeType.PROGRAM, lexer, asmSpec) as GASNode.Program

        nativeLog("Parsed File!")

        //nativeLog("AsmPsiParser parses file: $fileName!")

        return AsmFile(file, languageService, program)
    }

    fun reparseStatements(fromIndex: Int, toIndex: Int, asmFile: AsmFile): List<GASNode.Statement> {
        val lexer = asmSpec.createLexer("")
        val content = asmFile.file.getAsUTF8String().substring(fromIndex, toIndex)
        lexer.reset(content)
        val program = GASNode.buildNode(GASNodeType.PROGRAM, lexer, asmSpec) as GASNode.Program
        return program.getAllStatements()
    }

}