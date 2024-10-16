package cengine.lang.asm.run

import cengine.editor.annotation.Severity
import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.asm.AsmLang
import cengine.lang.asm.mif.MifBuilder
import cengine.psi.impl.PsiNotationCollector
import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

class AsmMif: RunConfiguration.FileRun<LanguageService> {
    override fun run(file: VirtualFile, lang: LanguageService, vfs: VFileSystem) {
        if (lang !is AsmLang) return
        val asmFile = lang.psiParser.parse(file)

        val outputPath = FPath.of(vfs, AsmLang.OUTPUT_DIR, AsmRelocatable.RELOCATABLE_SUB_DIR, file.name.removeSuffix(lang.fileSuffix) +".mif")

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val builder = MifBuilder(lang.spec.wordSize, lang.spec.memAddrSize)

        // TODO: Parse AST to Binary

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
            outputFile.setAsUTF8String(builder.build())
        }
    }

    override val name: String = "MIF"
}