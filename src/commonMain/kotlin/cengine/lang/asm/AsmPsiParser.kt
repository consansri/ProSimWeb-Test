package cengine.lang.asm

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.asm.elf.ELFBuilder
import cengine.lang.asm.elf.Ehdr
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog

class AsmPsiParser(val spec: TargetSpec, val languageService: AsmLang) : PsiParser {
    override fun parseFile(file: VirtualFile, textModel: TextModel?): AsmFile {
        nativeLog("Parsing file ...")

        val content = try {
            textModel?.toString() ?: file.getAsUTF8String()
        } catch (e: ConcurrentModificationException) {
            textModel?.toString() ?: file.getAsUTF8String()
        }

        val lexer = spec.createLexer(content)
        lexer.reset(content)

        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, spec) as ASNode.Program

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

    fun generateExecutable(file: AsmFile){
        val builder = ELFBuilder(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, Ehdr.ET_EXEC, spec.e_machine)
    }

    fun reparseStatements(fromIndex: Int, toIndex: Int, asmFile: AsmFile): List<ASNode.Statement> {
        val lexer = spec.createLexer("")
        val content = asmFile.file.getAsUTF8String().substring(fromIndex, toIndex)
        lexer.reset(content)
        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, spec) as ASNode.Program
        return program.getAllStatements()
    }

    private class SemanticAnalyzer() : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return
            when(element){
                is ASNode.Directive -> return element.type.checkSemantic(element)
                is ASNode.Instruction -> return element.type.checkSemantic(element)
                else -> {}
            }

            element.children.forEach {
                it.accept(this)
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