package cengine.lang.asm

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmTokenType
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

        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, asmSpec) as ASNode.Program

        program.accept(ParentLinker())

        val labelCollector = LabelCollector()
        program.accept(labelCollector)

        program.accept(LabelLinker(labelCollector.labels))

        val analyzer = SemanticAnalyzer()
        program.accept(analyzer)

        nativeLog("Parsed File!")

        //nativeLog("AsmPsiParser parses file: $fileName!")

        return AsmFile(file, languageService, program).apply {
            this.textModel = textModel
        }
    }

    fun reparseStatements(fromIndex: Int, toIndex: Int, asmFile: AsmFile): List<ASNode.Statement> {
        val lexer = asmSpec.createLexer("")
        val content = asmFile.file.getAsUTF8String().substring(fromIndex, toIndex)
        lexer.reset(content)
        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, asmSpec) as ASNode.Program
        return program.getAllStatements()
    }

    private class SemanticAnalyzer : PsiElementVisitor {

        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return
            when(element){
                is ASNode.ArgDef.Named -> TODO()
                is ASNode.ArgDef.Positional -> TODO()
                is ASNode.Argument.Basic -> TODO()
                is ASNode.Argument.DefaultValue -> TODO()
                is ASNode.Comment -> TODO()
                is ASNode.Directive -> TODO()
                is ASNode.Error -> TODO()
                is ASNode.Instruction -> TODO()
                is ASNode.Label -> TODO()
                is ASNode.NumericExpr.Classic -> TODO()
                is ASNode.NumericExpr.Operand.Char -> TODO()
                is ASNode.NumericExpr.Operand.Identifier -> TODO()
                is ASNode.NumericExpr.Operand.Number -> TODO()
                is ASNode.NumericExpr.Prefix -> TODO()
                is ASNode.Program -> TODO()
                is ASNode.Statement.Dir -> TODO()
                is ASNode.Statement.Empty -> TODO()
                is ASNode.Statement.Instr -> TODO()
                is ASNode.Statement.Unresolved -> TODO()
                is ASNode.StringExpr.Concatenation -> TODO()
                is ASNode.StringExpr.Operand.Identifier -> TODO()
                is ASNode.StringExpr.Operand.StringLiteral -> TODO()
                is ASNode.TokenExpr -> TODO()
            }

        }
    }



    private class LabelLinker(val labels: Set<ASNode.Label>) : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return

            when (element) {
                is ASNode.NumericExpr.Operand.Identifier -> {
                    val identifier = element.symbol.value.take(element.symbol.value.length - 1)
                    if (element.symbol.type == AsmTokenType.L_LABEL_REF) {
                        val reference = when {
                            element.symbol.value.endsWith("f", true) -> {
                                labels.firstOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.first > element.range.last && it.identifier == identifier }
                            }

                            element.symbol.value.endsWith("b", true) -> {
                                labels.lastOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.last < element.range.first && it.identifier == identifier }
                            }

                            else -> null
                        }
                        element.referencedElement = reference
                    } else {
                        val reference = labels.firstOrNull { it.identifier == element.symbol.value }
                        element.referencedElement = reference
                    }
                    return
                }

                is ASNode.StringExpr.Operand.Identifier -> {
                    val identifier = element.symbol.value.take(element.symbol.value.length - 1)
                    if (element.symbol.type == AsmTokenType.L_LABEL_REF) {
                        val reference = when {
                            element.symbol.value.endsWith("f", true) -> {
                                labels.firstOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.first > element.range.last && it.identifier == identifier }
                            }

                            element.symbol.value.endsWith("b", true) -> {
                                labels.lastOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.last < element.range.first && it.identifier == identifier }
                            }

                            else -> null
                        }
                        element.referencedElement = reference
                    } else {
                        val reference = labels.firstOrNull { it.identifier == element.symbol.value }
                        element.referencedElement = reference
                    }
                    return
                }

                else -> {}
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }

    private class LabelCollector : PsiElementVisitor {
        val labels: MutableSet<ASNode.Label> = mutableSetOf()

        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return

            when (element) {
                is ASNode.Label -> {
                    if (labels.firstOrNull { it.type != ASNode.Label.Type.NUMERIC && it.identifier == element.identifier } != null) {
                        element.notations.add(Notation.error(element, "Label is already defined!"))
                        return
                    }

                    labels.add(element)
                    return
                }

                is ASNode.Program -> {}
                is ASNode.Statement.Dir -> {}
                is ASNode.Statement.Empty -> {}
                is ASNode.Statement.Instr -> {}
                is ASNode.Statement.Unresolved -> {}
                else -> return
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }

    private class ParentLinker : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.parent = file
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return
            element.children.forEach {
                it.parent = element
                it.accept(this)
            }
        }
    }

}