package cengine.lang.obj.elf

import cengine.util.buffer.ByteBuffer

sealed class ELFLinker<ELFFILE : ELFFile<*, *, *, *, *, *, *>>(private val inputFiles: List<ELFFILE>) {

    class ELF32Linker(files: List<Pair<String, ByteArray>>) : ELFLinker<ELF32File>(
        files.mapNotNull { (name, content) ->
            ELFFile.parse(name, content) as? ELF32File
        }
    )

    class ELF64Linker(files: List<Pair<String, ByteArray>>) : ELFLinker<ELF64File>(
        files.mapNotNull { (name, content) ->
            ELFFile.parse(name, content) as? ELF64File
        }
    )

    private val outputBuffer: ByteBuffer = TODO()

    private val symbols = mutableMapOf<String, Sym>()
    private val sections = mutableMapOf<String, MutableList<ByteArray>>()

    private val mergedSections = mutableMapOf<String, ByteArray>()
    private val currentAddress = 0x08048000

    fun linkFiles(): ByteArray {
        TODO()
    }

    fun readAndParseInputFiles() {

    }

    fun performSymbolResolution() {

    }

    fun performSectionMerging() {

    }

    fun layoutOutputFile() {

    }

    fun performRelocation() {

    }

    fun createExecutableHeader() {

    }

    fun createProgramHeaderTable() {

    }

    fun createSectionHeaderTable() {

    }

    fun writeOutputFile() {

    }

    fun postProcess() {

    }

    class ELFLinkerException(message: String) : Exception(message)

}