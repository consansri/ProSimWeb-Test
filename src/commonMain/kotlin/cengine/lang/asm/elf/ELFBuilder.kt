package cengine.lang.asm.elf

import cengine.lang.asm.ast.impl.ASNode
import cengine.util.ByteBuffer
import cengine.util.Endianness
import cengine.vfs.VirtualFile

class ELFBuilder(
    val ei_class: Elf_Byte,
    val ei_data: Elf_Byte,
    val ei_osabi: Elf_Byte,
    val ei_abiversion: Elf_Byte,
    val e_type: Elf_Half,
    val e_machine: Elf_Half
) {
    private val e_ident: E_IDENT = E_IDENT(ei_class = ei_class, ei_data = ei_data, ei_osabi = ei_osabi, ei_abiversion = ei_abiversion)


    private val endianness = when (ei_data) {
        E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
        E_IDENT.ELFDATA2MSB -> Endianness.BIG
        else -> {
            throw ELFBuilderException("Invalid Data Encoding $ei_data.")
        }
    }

    private var entryPoint: ULong = 0x0UL

    private var currentSection: Section = Section(".text", endianness)

    private val sections: MutableSet<Section> = mutableSetOf(
        currentSection,
        Section(".data", endianness),
        Section(".bss", endianness),
        Section(".symtab", endianness)
    )

    private val programHeaderTable: MutableList<Phdr> = mutableListOf()

    private val sectionContents: MutableList<Byte> = mutableListOf()

    private val sectionHeaderTable: MutableList<Shdr> = mutableListOf()

    fun build(outputFile: VirtualFile, vararg statements: ASNode.Statement) {
        parseStatements(*statements)

        outputFile.setContent(writeELFFile())
    }

    private fun parseStatements(vararg statements: ASNode.Statement) {

    }

    private fun writeELFFile(): ByteArray {
        val byteBuffer = ByteBuffer(endianness)

        byteBuffer.writeELFHeader()
        byteBuffer.writeSHDRs()
        byteBuffer.writeSections()
        byteBuffer.writePHDRs()

        return byteBuffer.toByteArray()
    }

    private fun ByteBuffer.writeELFHeader() {

    }

    private fun ByteBuffer.writePHDRs() {

    }

    private fun ByteBuffer.writeSections() {
        sections.forEach {
            put(it.content.toByteArray())
        }
    }

    private fun ByteBuffer.writeSHDRs() {

    }


    class ELFBuilderException(message: String) : Exception(message)


    class Section(val name: String, endianness: Endianness) {
        val content = ByteBuffer(endianness)

        override fun equals(other: Any?): Boolean {
            if (other !is Section) return false
            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}