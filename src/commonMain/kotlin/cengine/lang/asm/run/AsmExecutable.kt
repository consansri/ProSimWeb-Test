package cengine.lang.asm.run

import cengine.editor.annotation.Severity
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ExecutableELFBuilder
import cengine.psi.impl.PsiNotationCollector
import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

class AsmExecutable : RunConfiguration.FileRun<LanguageService> {
    companion object {
        const val EXECUTABLE_SUB_DIR = "exec"
    }

    override val name: String = "ELF Executable"

    override fun run(file: VirtualFile, lang: LanguageService, vfs: VFileSystem) {
        if (lang !is AsmLang) return
        val asmFile = lang.psiParser.parse(file)

        val outputPath = FPath.of(vfs, AsmLang.OUTPUT_DIR, EXECUTABLE_SUB_DIR, file.name.removeSuffix(lang.fileSuffix) +".o")

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val builder = ExecutableELFBuilder(lang.spec)
        val content = builder.build(*asmFile.children.filterIsInstance<ASNode.Statement>().toTypedArray())

        val collector = PsiNotationCollector()
        asmFile.accept(collector)

        collector.annotations.forEach {
            when (it.severity) {
                Severity.INFO -> nativeInfo(it.createConsoleMessage(asmFile))
                Severity.WARNING -> nativeWarn(it.createConsoleMessage(asmFile))
                Severity.ERROR -> nativeError(it.createConsoleMessage(asmFile))
            }
        }

        if (collector.annotations.none { it.severity == Severity.ERROR }) {
            outputFile.setContent(content)
        }
    }
}