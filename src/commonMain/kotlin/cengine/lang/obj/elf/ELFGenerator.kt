package cengine.lang.obj.elf

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.AsmCodeGenerator.*
import cengine.lang.obj.elf.Shdr.Companion.SHF_ALLOC
import cengine.util.Endianness
import cengine.util.buffer.ByteBuffer
import cengine.util.buffer.ByteBuffer.Companion.toASCIIByteArray
import cengine.util.buffer.ByteBuffer.Companion.toASCIIString
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue

abstract class ELFGenerator(
    val e_type: Elf_Half,
    val ei_class: Elf_Byte,
    val ei_data: Elf_Byte,
    val ei_osabi: Elf_Byte,
    val ei_abiversion: Elf_Byte,
    val e_machine: Elf_Half,
    var e_flags: Elf_Word,
    linkerScript: LinkerScript,
) : AsmCodeGenerator<ELFGenerator.ELFSection>(linkerScript) {

    override val fileSuffix: String
        get() = ".o"

    val endianness: Endianness = when (ei_data) {
        E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
        E_IDENT.ELFDATA2MSB -> Endianness.BIG
        else -> {
            throw ELFBuilderException("Invalid Data Encoding $ei_data.")
        }
    }

    /**
     * ELF IDENTIFICATION & CLASSIFICATION
     */
    protected val e_ident: E_IDENT = E_IDENT(ei_class = ei_class, ei_data = ei_data, ei_osabi = ei_osabi, ei_abiversion = ei_abiversion)

    protected var entryPoint: Elf_Xword = 0U

    protected val segments: MutableList<Segment> = mutableListOf()

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
    override val sections: MutableList<ELFSection> = mutableListOf()

    val shStrTab = SHStrTab()
    val strTab = StrTab()

    private val nullSec = getOrCreateSection("")
    private val text = getOrCreateSection(".text", Shdr.SHT_text, Shdr.SHF_text.toULong())

    override var currentSection: ELFSection = text

    init {
        symbols.add(Symbol.Label("", nullSec, 0U))
    }

    override fun writeFile(): ByteArray {
        val buffer = ByteBuffer(endianness)

        sections.add(SymTab(strTab))
        sections.add(strTab)
        sections.add(shStrTab)

        val shdrs = calculateShdrs()

        val phdrSize = segments.firstOrNull()?.phdr?.byteSize()?.toULong() ?: 0U
        val phdrCount = segments.size.toULong()

        val ehdr = createELFHeader()

        val totalEhdrBytes = ehdr.byteSize().toULong()
        val totalPhdrBytes = phdrSize * phdrCount
        val totalSectionBytes = sections.sumOf { it.content.size.toULong() }
        val phoff = totalEhdrBytes
        val shoff = totalEhdrBytes + totalPhdrBytes + totalSectionBytes

        buffer.writeELFHeader(ehdr, phoff, shoff)
        buffer.writePHDRs(totalEhdrBytes + totalPhdrBytes)
        buffer.writeSections(shdrs)
        buffer.writeSHDRs(shdrs.map { it.second })

        return buffer.toArray().toByteArray()
    }

    private fun calculateShdrs(): List<Pair<ELFSection, Shdr>> {
        return sections.map {
            it to when (val shdr = it.createHeader()) {
                is ELF32_Shdr -> {
                    shdr.sh_size = it.content.size.toUInt()
                    shdr
                }

                is ELF64_Shdr -> {
                    shdr.sh_size = it.content.size.toULong()
                    shdr
                }
            }
        }
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
                e_phnum = segments.size.toUShort(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUShort(),
                e_shstrndx = sections.indexOf(shStrTab).toUShort(),
                e_entry = entryPoint.toUInt(),
                e_phoff = 0U, // assign laterc
                e_shoff = 0U // assign later
            )

            E_IDENT.ELFCLASS64 -> ELF64_Ehdr(
                e_ident = e_ident,
                e_type = e_type,
                e_machine = e_machine,
                e_flags = e_flags,
                e_ehsize = 0U, // assign later
                e_phentsize = Phdr.size(ei_class),
                e_phnum = segments.size.toUShort(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUShort(),
                e_shstrndx = sections.indexOf(shStrTab).toUShort(),
                e_entry = entryPoint,
                e_phoff = 0U, // assign later
                e_shoff = 0U // assign later
            )

            else -> throw InvalidElfClassException(e_ident.ei_class)
        }
        ehdr.e_ehsize = ehdr.byteSize().toUShort()
        return ehdr
    }

    private fun ByteBuffer.writeELFHeader(ehdr: Ehdr, phoff: ULong, shoff: ULong) {
        when (ehdr) {
            is ELF32_Ehdr -> {
                if (segments.isNotEmpty()) ehdr.e_phoff = phoff.toUInt()
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff.toUInt()
            }

            is ELF64_Ehdr -> {
                if (segments.isNotEmpty()) ehdr.e_phoff = phoff
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff
            }
        }

        putAll(ehdr.build(endianness))
    }

    private fun ByteBuffer.writePHDRs(fileIndexOfDataStart: ULong) {
        // Serialize the program header (Phdr) into the byte buffer
        segments.forEach { segment ->
            // Set FileOffset of Segment Start
            val firstSection = segment.sections.firstOrNull()
            segment.p_offset = if (firstSection != null) {
                fileIndexOfDataStart + sections.takeWhile { it != firstSection }.sumOf { it.content.size.toULong() }
            } else fileIndexOfDataStart

            putAll(segment.phdr.build(endianness))
        }
    }

    private fun ByteBuffer.writeSections(shdr: List<Pair<ELFSection, Shdr>>) {
        shdr.forEach { (section, header) ->
            val start = size
            putAll(section.content.toArray())
            when (header) {
                is ELF32_Shdr -> {
                    header.sh_offset = start.toUInt()
                }

                is ELF64_Shdr -> {
                    header.sh_offset = start.toULong()
                }
            }
        }
    }

    private fun ByteBuffer.writeSHDRs(shdrs: List<Shdr>) {
        shdrs.forEachIndexed { index, shdr ->
            putAll(shdr.build(endianness))
        }
    }

    protected fun createAndAddSegment(p_type: Elf_Word, p_flags: Elf_Word, p_align: ULong = 1U): Segment {
        val phdr = when (ei_class) {
            E_IDENT.ELFCLASS32 -> {
                ELF32_Phdr(
                    p_type = p_type,
                    p_flags = p_flags,
                    p_align = p_align.toUInt()
                )
            }

            E_IDENT.ELFCLASS64 -> {
                ELF64_Phdr(
                    p_type = p_type,
                    p_flags = p_flags,
                    p_align = p_align
                )
            }

            else -> throw InvalidElfClassException(ei_class)
        }
        return Segment(phdr).also { segment ->
            segments.add(segment)
        }
    }

    final override fun createNewSection(name: String, type: UInt, flags: ULong, link: ELFSection?, info: String?): ELFSection = ELFSection(name, type, flags, link, info)

    fun Symbol<ELFSection>.toSym(): Sym {
        val binding = when (this.binding) {
            Symbol.Binding.LOCAL -> Sym.STB_LOCAL
            Symbol.Binding.GLOBAL -> Sym.STB_GLOBAL
            Symbol.Binding.WEAK -> Sym.STB_WEAK
        }
        val sym =
            when (this) {
                is Symbol.Abs -> {
                    Sym.createEmpty(ei_class, strTab.addString(this.name), sections.indexOf(section).toUShort(), type = Sym.STT_NUM, binding = binding).apply {
                        setValue(value)
                    }
                }

                is Symbol.Label -> {
                    Sym.createEmpty(ei_class, strTab.addString(this.name), sections.indexOf(section).toUShort(), type = Sym.STT_NOTYPE, binding = binding).apply {
                        setValue(address().toULong())
                    }
                }
            }
        return sym
    }

    // CLASSES


    /**
     * This section holds a symbol table, as "Symbol Table'' in this chapter
     * describes. If a file has a loadable segment that includes the symbol table,
     * the section's attributes will include the [SHF_ALLOC] bit; otherwise, that bit
     * will be off.
     */
    inner class SymTab(strTab: StrTab) : ELFSection(
        ".symtab",
        Shdr.SHT_SYMTAB,
        link = strTab,
        entSize = Sym.size(ei_class).toULong()
    ) {
        override val name = ".symtab"
        override val content: ByteBuffer = ByteBuffer(endianness).apply {
            symbols.forEach {
                putBytes(it.toSym().build(endianness))
            }
        }
        override val reservations: MutableList<InstrReservation> = mutableListOf()
    }

    /**
     * This section holds section names.
     */
    inner class SHStrTab : ELFSection(
        ".shstrtab",
        Shdr.SHT_STRTAB
    ) {
        override val name = ".shstrtab"
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()
        private val stringIndexMap = mutableMapOf<String, UInt>()

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
    inner class StrTab : ELFSection(
        ".strtab",
        Shdr.SHT_STRTAB
    ) {

        override val name = ".strtab"
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()
        private val stringIndexMap = mutableMapOf<String, UInt>()

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

    open inner class ELFSection(
        override val name: String,
        override var type: UInt,
        override var flags: ULong = 0U,
        override var link: Section? = null,
        override var info: String? = null,
        private val entSize: ULong? = null,
    ) : Section {
        override var address: Hex = Hex("0", Size.Bit64)

        fun createHeader(): Shdr = Shdr.create(ei_class).apply {
            sh_name = shStrTab[name] ?: shStrTab.addString(name)
            sh_type = type

            setAddr(address.toULong())
            setFlags(flags)

            val currLink = link
            if (currLink != null) {
                sh_link = sections.indexOf(currLink).toUInt()
            }
            val currInfo = info
            if (currInfo != null) {
                sh_info = strTab.get(currInfo) ?: strTab.addString(currInfo)
            }
            if (entSize != null) {
                setEntSize(entSize)
            }
        }

        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()

        override fun toString(): String = print()
    }

    // INTERFACES


    class Segment(val phdr: Phdr) {
        val sections: MutableList<ELFSection> = mutableListOf()

        var p_offset: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_offset = value.toUInt()
                    is ELF64_Phdr -> phdr.p_offset = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toULong()
                is ELF64_Phdr -> phdr.p_offset
            }

        var p_vaddr: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_vaddr = value.toUInt()
                    is ELF64_Phdr -> phdr.p_vaddr = value
                }
                calculateSectionAddresses()
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_vaddr.toULong()
                is ELF64_Phdr -> phdr.p_vaddr
            }

        var p_paddr: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_paddr = value.toUInt()
                    is ELF64_Phdr -> phdr.p_paddr = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_paddr.toULong()
                is ELF64_Phdr -> phdr.p_paddr
            }

        val p_align: Elf_Xword
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_align.toULong()
                is ELF64_Phdr -> phdr.p_align
            }

        var p_filesz: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_filesz = value.toUInt()
                    is ELF64_Phdr -> phdr.p_filesz = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toULong()
                is ELF64_Phdr -> phdr.p_filesz
            }

        var p_memsz: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_memsz = value.toUInt()
                    is ELF64_Phdr -> phdr.p_memsz = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_memsz.toULong()
                is ELF64_Phdr -> phdr.p_memsz
            }

        // Adds a section to this segment
        fun addSection(section: ELFSection) {
            sections.add(section)
            calculateSegmentSize()
            calculateSectionAddresses()
        }

        // Calculates the size of the segment based on the sections added
        private fun calculateSegmentSize() {
            val size = sections.sumOf { it.content.size.toULong() }
            p_filesz = size
            p_memsz = size
        }

        // Sets the addresses of each section within the segment based on the segment's start address
        private fun calculateSectionAddresses() {
            var currentAddress = p_vaddr
            sections.forEach { section ->
                section.address = currentAddress.toValue()
                currentAddress += section.content.size.toULong()
            }
        }

        override fun toString(): String {
            return "${Phdr.getProgramHeaderType(phdr.p_type)},${Phdr.getProgramHeaderFlags(phdr.p_flags)}: ${sections.joinToString { it.print() }}"
        }
    }

    // EXCEPTIONS

    open class ELFBuilderException(message: String) : Exception(message)

    class InvalidElfClassException(ei_class: Elf_Byte) : ELFBuilderException("Invalid ELF Class $ei_class.")
    class InvalidElfDataException(ei_data: Elf_Byte) : ELFBuilderException("Invalid ELF Data $ei_data type.")


}