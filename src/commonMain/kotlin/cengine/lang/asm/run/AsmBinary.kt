package cengine.lang.asm.run

import cengine.editor.annotation.Severity
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.BinaryBuilder
import cengine.psi.impl.PsiNotationCollector
import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

class AsmBinary: RunConfiguration.FileRun<LanguageService> {
    override fun run(file: VirtualFile, lang: LanguageService, vfs: VFileSystem) {
        if (lang !is AsmLang) return
        val asmFile = lang.psiParser.parse(file)

        val outputPath = FPath.of(vfs, AsmLang.OUTPUT_DIR, AsmRelocatable.RELOCATABLE_SUB_DIR, file.name.removeSuffix(lang.fileSuffix) +".o")

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val builder = BinaryBuilder(lang.spec)
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

    override val name: String = "Binary"
}