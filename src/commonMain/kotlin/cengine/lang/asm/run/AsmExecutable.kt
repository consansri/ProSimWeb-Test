package cengine.lang.asm.run

import cengine.editor.annotation.Severity
import cengine.lang.RunConfiguration
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder
import cengine.lang.asm.elf.Ehdr
import cengine.psi.impl.PsiNotationCollector
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import emulator.kit.nativeError
import emulator.kit.nativeInfo
import emulator.kit.nativeWarn

class AsmExecutable(val asmLang: AsmLang) : RunConfiguration.FileRun {
    companion object {
        const val EXECUTABLE_SUB_DIR = "exec"
    }

    override val name: String = "ELF Executable"
    private val spec: TargetSpec get() = asmLang.spec

    override fun run(file: VirtualFile, vfs: VFileSystem) {
        val asmFile = asmLang.psiParser.parseFile(file, null)

        val outputPath = "${AsmLang.OUTPUT_DIR}${VFileSystem.DELIMITER}$EXECUTABLE_SUB_DIR${VFileSystem.DELIMITER}${file.name.removeSuffix(asmLang.fileSuffix)}.o"

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val builder = ELFBuilder(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, Ehdr.ET_EXEC, spec.e_machine)
        val content = builder.build(*asmFile.children.filterIsInstance<ASNode.Statement>().toTypedArray())

        val collector = PsiNotationCollector()
        asmFile.accept(collector)

        collector.notations.forEach {
            when (it.severity) {
                Severity.INFO -> nativeInfo(it.createConsoleMessage(asmFile))
                Severity.WARNING -> nativeWarn(it.createConsoleMessage(asmFile))
                Severity.ERROR -> nativeError(it.createConsoleMessage(asmFile))
            }
        }

        if (collector.notations.filter { it.severity == Severity.ERROR }.isEmpty()) {
            outputFile.setContent(content)
        }
    }


}