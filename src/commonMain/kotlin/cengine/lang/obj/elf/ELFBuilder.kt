package cengine.lang.obj.elf

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.obj.elf.Shdr.Companion.SHF_ALLOC
import cengine.util.ByteBuffer
import cengine.util.ByteBuffer.Companion.toASCIIByteArray
import cengine.util.ByteBuffer.Companion.toASCIIString
import cengine.util.Endianness

abstract class ELFBuilder(
    val e_type: Elf_Half,
    val ei_class: Elf_Byte,
    val ei_data: Elf_Byte,
    val ei_osabi: Elf_Byte,
    val ei_abiversion: Elf_Byte,
    val e_machine: Elf_Half,
    var e_flags: Elf_Word
) {

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
    protected val sections: MutableList<Section> = mutableListOf()

    val shStrTab = SHStrTab()

    val strTab = StrTab()

    val symTab = SymTab()

    val rels = mutableListOf<RelTab>()
    val relas = mutableListOf<RelaTab>()

    private val nullSec = createNewSection("").also {
        sections.add(0, it)
    }

    val text = getOrCreateSection(".text", Shdr.SHT_text).apply {
        shdr.setFlags(Shdr.SHF_text.toULong())
    }

    val rodata = getOrCreateSection(".rodata", Shdr.SHT_rodata).apply {
        shdr.setFlags(Shdr.SHF_rodata.toULong())
    }

    val data = getOrCreateSection(".data", Shdr.SHT_data).apply {
        shdr.setFlags(Shdr.SHF_data.toULong())
    }

    val bss = getOrCreateSection(".bss", Shdr.SHT_bss).apply {
        shdr.setFlags(Shdr.SHF_bss.toULong())
    }

    var currentSection: Section = text

    abstract fun build(vararg statements: ASNode.Statement): ByteArray

    protected abstract fun Section.resolveReservations()

    protected fun writeELFFile(): ByteArray {
        val buffer = ByteBuffer(endianness)

        sections.addAll(rels)
        sections.addAll(relas)
        sections.add(symTab)
        sections.add(strTab)
        sections.add(shStrTab)

        val phdrSize = segments.firstOrNull()?.phdr?.byteSize()?.toULong() ?: 0U
        val phdrCount = segments.size.toULong()

        val ehdr = createELFHeader()

        val totalEhdrBytes = ehdr.byteSize().toULong()
        val totalPhdrBytes = phdrSize * phdrCount
        val totalSectionBytes = sections.sumOf { it.content.size.toULong() }
        val shstrndx = sections.indexOf(shStrTab).toUShort()
        val phoff = totalEhdrBytes
        val shoff = totalEhdrBytes + totalPhdrBytes + totalSectionBytes

        buffer.writeELFHeader(ehdr, shstrndx, phoff, shoff)
        buffer.writePHDRs(totalEhdrBytes + totalPhdrBytes)
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
                e_phnum = segments.size.toUShort(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUShort(),
                e_shstrndx = 0U, // assign later
                e_entry = 0U, // assign later
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
                if (segments.isNotEmpty()) ehdr.e_phoff = phoff.toUInt()
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff.toUInt()
            }

            is ELF64_Ehdr -> {
                ehdr.e_entry = entryPoint
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

    private fun ByteBuffer.writeSections() {
        symTab.shdr.sh_link = sections.indexOf(strTab).toUInt()
        relas.forEach {
            it.shdr.sh_link = sections.indexOf(symTab).toUInt()
        }
        rels.forEach {
            it.shdr.sh_link = sections.indexOf(symTab).toUInt()
        }

        sections.forEach {
            val start = size
            putAll(it.content.toByteArray())
            when (val shdr = it.shdr) {
                is ELF32_Shdr -> {
                    shdr.sh_offset = start.toUInt()
                    shdr.sh_size = it.content.size.toUInt()
                }

                is ELF64_Shdr -> {
                    shdr.sh_offset = start.toULong()
                    shdr.sh_size = it.content.size.toULong()
                }
            }
        }
    }

    private fun ByteBuffer.writeSHDRs() {
        sections.forEachIndexed { index, sec ->
            putAll(sec.shdr.build(endianness))
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

    private fun getOrCreateSection(name: String, type: Elf_Word? = null, link: Elf_Word? = null, info: String? = null): Section {
        val section = sections.firstOrNull { it.shdr.sh_name == shStrTab[name] }
        if (section != null) return section
        val created = createNewSection(name, type, link, info)
        sections.add(created)
        return created
    }

    private fun createNewSection(name: String, type: Elf_Word? = null, link: Elf_Word? = null, info: String? = null): Section = object : Section {
        override val name: String = name
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()

        init {
            shdr.sh_name = shStrTab.addString(name)
            if (type != null) shdr.sh_type = type
            if (link != null) shdr.sh_link = link
            if (info != null) shdr.sh_info = strTab.addString(info)
        }
    }

    protected fun getOrCreateRel(section: Section): RelTab {
        val searched = rels.firstOrNull { it.relocatable == section }
        if (searched != null) return searched
        val rel = RelTab(section)
        rels.add(rel)
        return rel
    }

    protected fun getOrCreateRela(section: Section): RelaTab {
        val searched = relas.firstOrNull { it.relocatable == section }
        if (searched != null) return searched
        val rel = RelaTab(section)
        relas.add(rel)
        return rel
    }

    fun addRelaEntry(identifier: String, type: Elf_Word, addend: Elf_Sxword, section: Section, offset: Elf_Word) {
        val relaTab = getOrCreateRela(section)
        relaTab.addEntry(identifier, type, addend, section, offset)
    }

    fun addRelEntry(identifier: String, type: Elf_Word, section: Section, offset: Elf_Word) {
        val relTab = getOrCreateRel(section)
        relTab.addEntry(identifier, type, section, offset)
    }

    // CLASSES

    /**
     * This section holds a symbol table, as "Symbol Table'' in this chapter
     * describes. If a file has a loadable segment that includes the symbol table,
     * the section's attributes will include the [SHF_ALLOC] bit; otherwise, that bit
     * will be off.
     */
    inner class SymTab : Section {
        override val name = ".symtab"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()

        private val symbols: MutableList<Sym> = mutableListOf()
        val symbolSize = Sym.size(ei_class)

        init {
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_SYMTAB
            shdr.setEntSize(Sym.size(ei_class).toULong())
            addSymbol(Sym.createEmpty(ei_class, strTab.addString(""), 0U)) // Adding UND Symbol
        }

        operator fun get(index: UInt): Sym = symbols.get(index.toInt())

        fun getOrCreate(identifier: String, section: Section): UInt {
            return search(identifier) ?: addSymbol(
                Sym.createEmpty(
                    ei_class,
                    strTab.addString(identifier),
                    sections.indexOf(section).toUShort(),
                )
            )
        }

        fun search(identifier: String): UInt? {
            val index = symbols.indexOfFirst { strTab.getStringAt(it.st_name) == identifier }
            if (index == -1) return null
            return index.toUInt()
        }

        fun update(newSym: Sym, index: UInt) {
            val newContent = newSym.build(endianness)
            for (i in 0..<symbolSize.toInt()) {
                content[index.toInt() * shdr.getEntSize().toInt() + i] = newContent[i]
            }
        }

        fun addSymbol(symbol: Sym): UInt {
            val index = symbols.size.toUInt()
            if (Sym.ELF_ST_BIND(symbol.st_info) == Sym.STB_LOCAL) {
                shdr.sh_info += 1U
            }
            symbols.add(symbol)
            content.putAll(symbol.build(endianness))
            return index
        }
    }

    /**
     * This section holds section names.
     */
    inner class SHStrTab : Section {
        override val name = ".shstrtab"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
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
    inner class StrTab : Section {

        override val name = ".strtab"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
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

    inner class RelTab(val relocatable: Section) : Section {
        override val name = ".rel" + relocatable.name
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
        val rels: MutableSet<Section.RelDef> = mutableSetOf()

        init {
            shdr.sh_name = shStrTab.get(name) ?: shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_REL
            shdr.setEntSize(Rel.size(ei_class).toULong())
            shdr.sh_info = sections.indexOf(relocatable).toUInt()
        }

        fun addEntry(identifier: String, type: Elf_Word, section: Section, offset: UInt) {
            val symIndex = symTab.search(identifier) ?: symTab.addSymbol(
                Sym.createEmpty(
                    ei_class,
                    strTab.addString(identifier),
                    sections.indexOf(section).toUShort()
                )
            )

            val rel = when (ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    ELF32_Rel(
                        offset,
                        ELF32_Rel.R_INFO(symIndex, type)
                    )
                }

                E_IDENT.ELFCLASS64 -> {
                    ELF64_Rel(
                        offset.toULong(),
                        ELF64_Rel.R_INFO(symIndex.toULong(), type.toULong())
                    )
                }

                else -> throw InvalidElfClassException(ei_class)
            }

            rels.add(Section.RelDef(rel, section, content.size.toUInt()))
            content.putAll(rel.build(endianness))
        }
    }

    inner class RelaTab(val relocatable: Section) : Section {
        override val name = ".rela" + relocatable.name
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
        val relas: MutableSet<Section.RelaDef> = mutableSetOf()

        init {
            shdr.sh_name = shStrTab.get(name) ?: shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_RELA
            shdr.setEntSize(Rela.size(ei_class).toULong())
            shdr.sh_info = sections.indexOf(relocatable).toUInt()
        }

        fun addEntry(identifier: String, type: Elf_Word, addend: Elf_Sxword, section: Section, offset: Elf_Word) {
            val sym = symTab.search(identifier) ?: symTab.addSymbol(
                Sym.createEmpty(
                    ei_class,
                    strTab.addString(identifier),
                    sections.indexOf(section).toUShort()
                )
            )

            val rel = when (ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    ELF32_Rela(
                        offset,
                        ELF32_Rel.R_INFO(sym, type),
                        addend.toInt()
                    )
                }

                E_IDENT.ELFCLASS64 -> {
                    ELF64_Rela(
                        offset.toULong(),
                        ELF64_Rel.R_INFO(sym.toULong(), type.toULong()),
                        addend
                    )
                }

                else -> throw InvalidElfClassException(ei_class)
            }

            relas.add(Section.RelaDef(rel, section, content.size.toUInt()))
            content.putAll(rel.build(endianness))
        }
    }

    // INTERFACES

    interface Section {
        val name: String
        val shdr: Shdr
        val content: ByteBuffer
        val reservations: MutableList<InstrReservation>
        val labels: MutableSet<LabelDef>

        fun isText(): Boolean {
            val shdr = shdr
            return shdr.sh_type == Shdr.SHT_PROGBITS && when (shdr) {
                is ELF32_Shdr -> shdr.sh_flags and Shdr.SHF_EXECINSTR != 0U
                is ELF64_Shdr -> shdr.sh_flags.toUInt() and Shdr.SHF_EXECINSTR != 0U
            }
        }

        fun isData(): Boolean {
            val shdr = shdr
            return (shdr.sh_type == Shdr.SHT_PROGBITS) || ((shdr.sh_type == Shdr.SHT_NOBITS)) && when (shdr) {
                is ELF32_Shdr -> shdr.sh_flags and (SHF_ALLOC + Shdr.SHF_WRITE) != 0U
                is ELF64_Shdr -> shdr.sh_flags.toUInt() and (SHF_ALLOC + Shdr.SHF_WRITE) != 0U
            }
        }

        fun isRoData(): Boolean {
            val shdr = shdr
            return shdr.sh_type == Shdr.SHT_PROGBITS && when (shdr) {
                is ELF32_Shdr -> shdr.sh_flags and SHF_ALLOC != 0U
                is ELF64_Shdr -> shdr.sh_flags.toUInt() and SHF_ALLOC != 0U
            }
        }

        fun addLabel(label: ASNode.Label) {
            labels.add(LabelDef(label, content.size.toUInt()))
        }

        fun queueLateInit(instr: ASNode.Instruction, byteAmount: Int) {
            reservations.add(InstrReservation(instr, content.size.toUInt()))
            content.putAll(ByteArray(byteAmount) { 0 })
        }

        data class LabelDef(val label: ASNode.Label, val offset: Elf_Word)
        data class InstrReservation(val instr: ASNode.Instruction, val offset: Elf_Word)
        data class SymbolDef(val symbol: Sym, val offset: Elf_Word)
        data class RelDef(val rel: Rel, val section: Section, val offset: Elf_Word)
        data class RelaDef(val rela: Rela, val section: Section, val offset: Elf_Word)
    }

    class Segment(val phdr: Phdr) {
        val sections: MutableList<Section> = mutableListOf()

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
        fun addSection(section: Section) {
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
                section.shdr.setAddr(currentAddress)
                currentAddress += section.content.size.toULong()
            }
        }

    }

    // EXCEPTIONS

    open class ELFBuilderException(message: String) : Exception(message)

    class InvalidElfClassException(ei_class: Elf_Byte) : ELFBuilderException("Invalid ELF Class $ei_class.")
    class InvalidElfDataException(ei_data: Elf_Byte) : ELFBuilderException("Invalid ELF Data $ei_data type.")


}