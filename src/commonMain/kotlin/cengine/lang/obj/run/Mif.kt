package cengine.lang.obj.run

import cengine.lang.LanguageService
import cengine.lang.RunConfiguration
import cengine.lang.obj.ObjLang
import cengine.lang.obj.elf.ELFFile
import cengine.lang.mif.MifConverter
import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile

object Mif : RunConfiguration.FileRun<LanguageService> {

    const val MIF_SUB_DIR = "mif"

    override fun run(file: VirtualFile, lang: LanguageService, vfs: VFileSystem) {
        if (lang !is ObjLang) return
        val outputPath = FPath.of(vfs, ObjLang.OUTPUT_DIR, MIF_SUB_DIR, file.name.removeSuffix(lang.fileSuffix) + ".mif")

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val elfFile = ELFFile.parse(file.name, file.getContent())
        if (elfFile != null) {
            val fileContent = MifConverter.parseElf(elfFile).build()
            outputFile.setAsUTF8String(fileContent)
        }
    }

    override val name: String = "MIF"
}