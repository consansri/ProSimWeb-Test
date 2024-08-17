package cengine.lang.asm.elf

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder.Section
import cengine.lang.asm.elf.Shdr.Companion.SHF_ALLOC
import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf32.ELF32_Shdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Shdr
import cengine.util.ByteBuffer
import cengine.util.ByteBuffer.Companion.toASCIIByteArray
import cengine.util.ByteBuffer.Companion.toASCIIString
import cengine.util.Endianness
import cengine.vfs.VirtualFile

/**
 * ELF File
 *
 * 1. [Ehdr]
 * 2. [Phdr]s
 * 3. [Section]s
 * 4. [Shdr]s
 */
class ELFBuilder(
    private val ei_class: Elf_Byte,
    private val ei_data: Elf_Byte,
    private val ei_osabi: Elf_Byte,
    private val ei_abiversion: Elf_Byte,
    private val e_type: Elf_Half,
    private val e_machine: Elf_Half,
    private var e_flags: Elf_Word = 0U
) {

    /**
     * ELF IDENTIFICATION & CLASSIFICATION
     */
    private val e_ident: E_IDENT = E_IDENT(ei_class = ei_class, ei_data = ei_data, ei_osabi = ei_osabi, ei_abiversion = ei_abiversion)

    private val endianness = when (ei_data) {
        E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
        E_IDENT.ELFDATA2MSB -> Endianness.BIG
        else -> {
            throw ELFBuilderException("Invalid Data Encoding $ei_data.")
        }
    }

    private val phdrs: MutableList<Phdr> = mutableListOf()

    private var entryPoint: ULong = 0x0UL

    /**
     * Sections Stored in the following order.
     *
     * 1. [nullSec]: (Empty Always at the beginning on ELF Sections)
     * 2. [text]: Executable code.
     * 3. [rodata]: Read-only data.
     * 4. [data]: Initialized writable data.
     * 5. [bss]: Uninitialized writable data.
     * 6. [plt] and [got]: Procedure linkage and global offset tables for dynamic linking.
     * 7. [dynsym] and [dynstr]: Dynamic symbol and string tables for dynamic linking.
     * 8. [rel] or [rela]: Relocation section.
     * 9. [shStrTab], [strTab], [symTab]: Section and symbol tables (for linking and debugging).
     */
    private val sections: MutableList<Section> = mutableListOf()
    private var endingSections = 0

    private val nullSec = createNewSection().also {
        sections.add(it)
    }

    private val shStrTab = SHStrTab().also {
        sections.add(it)
        ++endingSections
    }

    private val strTab = StrTab().also {
        sections.add(it)
        ++endingSections
    }

    private val symTab = SymTab().also {
        sections.add(it)
        ++endingSections
    }

    private val text = getOrCreateSection(".text", Shdr.SHT_text).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_text
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_text.toULong()
        }
    }

    private val rodata = createNewSection(".rodata", Shdr.SHT_rodata).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_rodata
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_rodata.toULong()
        }
    }

    private val data = createNewSection(".data", Shdr.SHT_data).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_data
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_data.toULong()
        }
    }

    private val bss = createNewSection(".bss", Shdr.SHT_bss).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_bss
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_bss.toULong()
        }
    }

    //

    private var currentSection: Section = text


    // PUBLIC METHODS

    fun build(outputFile: VirtualFile, vararg statements: ASNode.Statement) {
        statements.forEach {
            it.execute()
        }

        outputFile.setContent(writeELFFile())
    }

    // PRIVATE METHODS

    private fun ASNode.Statement.execute() {

    }

    private fun writeELFFile(): ByteArray {
        val buffer = ByteBuffer(endianness)

        val phdrSize = phdrs.firstOrNull()?.byteSize()?.toULong() ?: 0U
        val phdrCount = phdrs.size.toULong()

        val ehdr = createELFHeader()

        val totalEhdrBytes = ehdr.byteSize().toULong()
        val totalPhdrBytes = phdrSize * phdrCount
        val totalSectionBytes = sections.sumOf { it.content.size.toULong() }
        val shstrndx = (totalEhdrBytes + totalPhdrBytes + totalSectionBytes + sections.takeWhile { it != shStrTab }.size.toULong() * Shdr.size(ei_class).toULong()).toUShort()
        val phoff = totalEhdrBytes
        val shoff = totalEhdrBytes + totalPhdrBytes + totalSectionBytes

        buffer.writeELFHeader(ehdr, shstrndx, phoff, shoff)
        buffer.writePHDRs()
        buffer.writeSections()
        buffer.writeSHDRs()

        return buffer.toByteArray()
    }

    private fun createELFHeader(): Ehdr {
        val ehdr = when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> ELF32_Ehdr(
                e_ident = e_ident,
                e_type = e_type,
                e_machine = e_machine,
                e_flags = e_flags,
                e_ehsize = 0U, // assign later
                e_phentsize = Phdr.size(ei_class),
                e_phnum = phdrs.size.toUShort(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUShort(),
                e_shstrndx = 0U, // assign later
                e_entry = 0U, // assign later
                e_phoff = 0U, // assign later
                e_shoff = 0U // assign later
            )

            E_IDENT.ELFCLASS64 -> ELF64_Ehdr(
                e_ident = e_ident,
                e_type = e_type,
                e_machine = e_machine,
                e_flags = e_flags,
                e_ehsize = 0U, // assign later
                e_phentsize = Phdr.size(ei_class),
                e_phnum = phdrs.size.toUShort(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUShort(),
                e_shstrndx = 0U, // assign later
                e_entry = 0U, // assign later
                e_phoff = 0U, // assign later
                e_shoff = 0U // assign later
            )

            else -> throw InvalidElfClassException(e_ident.ei_class)
        }
        ehdr.e_ehsize = ehdr.byteSize().toUShort()
        return ehdr
    }

    private fun ByteBuffer.writeELFHeader(ehdr: Ehdr, shstrndx: UShort, phoff: ULong, shoff: ULong) {
        ehdr.e_shstrndx = shstrndx
        when (ehdr) {
            is ELF32_Ehdr -> {
                ehdr.e_entry = entryPoint.toUInt()
                if (phdrs.isNotEmpty()) ehdr.e_phoff = phoff.toUInt()
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff.toUInt()
            }

            is ELF64_Ehdr -> {
                ehdr.e_entry = entryPoint
                if (phdrs.isNotEmpty()) ehdr.e_phoff = phoff
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff
            }
        }

        putAll(ehdr.build(endianness))
    }

    private fun ByteBuffer.writePHDRs() {

    }

    private fun ByteBuffer.writeSections() {
        sections.forEach {
            putAll(it.content.toByteArray())
        }
    }

    private fun ByteBuffer.writeSHDRs() {
        sections.forEach {
            putAll(it.shdr.build(endianness))
        }
    }

    private fun getOrCreateSection(name: String, type: Elf_Word? = null, link: Elf_Word? = null, info: String? = null): Section {
        val section = sections.firstOrNull { it.shdr.sh_name == shStrTab[name] }
        if (section != null) return section
        val created = createNewSection(name, type, link, info)
        sections.add(sections.size - endingSections, created)
        return created
    }

    private fun createNewSection(name: String? = null, type: Elf_Word? = null, link: Elf_Word? = null, info: String? = null): Section = object : Section {
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)

        init {
            if (name != null) shdr.sh_name = shStrTab.addString(name)
            if (type != null) shdr.sh_type = type
            if (link != null) shdr.sh_link = link
            if (info != null) shdr.sh_info = strTab.addString(info)
        }
    }

    // CLASSES

    /**
     * This section holds a symbol table, as "Symbol Table'' in this chapter
     * describes. If a file has a loadable segment that includes the symbol table,
     * the section's attributes will include the [SHF_ALLOC] bit; otherwise, that bit
     * will be off.
     */
    inner class SymTab() : Section {
        val name = ".symtab"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)

        init {
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_SYMTAB
        }

        fun addSymbol() {
            TODO()
        }
    }

    /**
     * This section holds section names.
     */
    inner class SHStrTab() : Section {
        val name = ".shstrtab"
        override val shdr: Shdr = Shdr.create(ei_class)

        override val content: ByteBuffer = ByteBuffer(endianness)
        private val stringIndexMap = mutableMapOf<String, UInt>()

        init {
            content.put((0).toByte())
            shdr.sh_name = addString(name)
            shdr.sh_type = Shdr.SHT_STRTAB
        }

        fun addString(str: String): UInt {
            return stringIndexMap.getOrPut(str) {
                val currentOffset = content.size
                content.putAll(str.toASCIIByteArray() + (0).toByte())
                currentOffset.toUInt()
            }
        }

        operator fun get(string: String): UInt? = stringIndexMap[string]

        fun getStringAt(index: UInt): String = content.getZeroTerminated(index.toInt()).toASCIIString()
    }

    /**
     * This section holds strings, most commonly the strings that represent the names
     * associated with symbol table entries. If a file has a loadable segment that
     * includes the symbol string table, the section's attributes will include the
     * [SHF_ALLOC] bit; otherwise, that bit will be off.
     */
    inner class StrTab() : Section {

        val name = ".strtab"
        override val shdr: Shdr = Shdr.create(ei_class)

        override val content: ByteBuffer = ByteBuffer(endianness)
        private val stringIndexMap = mutableMapOf<String, UInt>()

        init {
            content.put((0).toByte())
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_STRTAB
        }

        fun addString(str: String): UInt {
            return stringIndexMap.getOrPut(str) {
                val currentOffset = content.size
                content.putAll(str.toASCIIByteArray() + (0).toByte())
                currentOffset.toUInt()
            }
        }

        operator fun get(string: String): UInt? = stringIndexMap[string]

        fun getStringAt(index: UInt): String = content.getZeroTerminated(index.toInt()).toASCIIString()
    }

    // INTERFACES

    interface Section {
        val shdr: Shdr
        val content: ByteBuffer
    }

    // EXCEPTIONS

    open class ELFBuilderException(message: String) : Exception(message)

    class InvalidElfClassException(ei_class: Elf_Byte) : ELFBuilderException("Invalid ELF Class $ei_class.")
}