package cengine.lang.asm.elf

import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder.Section
import cengine.lang.asm.elf.Shdr.Companion.SHF_ALLOC
import cengine.lang.asm.elf.elf32.*
import cengine.lang.asm.elf.elf64.*
import cengine.psi.core.*
import cengine.util.ByteBuffer
import cengine.util.ByteBuffer.Companion.toASCIIByteArray
import cengine.util.ByteBuffer.Companion.toASCIIString
import cengine.util.Endianness
import kotlin.experimental.*

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
    private var e_flags: Elf_Word
) {

    constructor(spec: TargetSpec, type: Elf_Half, e_flags: Elf_Word = 0U) : this(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, type, spec.e_machine, e_flags)

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

    val shStrTab = SHStrTab().also {
        sections.add(it)
        ++endingSections
    }

    val strTab = StrTab().also {
        sections.add(sections.size - endingSections, it)
        ++endingSections
    }

    val symTab = SymTab().also {
        sections.add(sections.size - endingSections, it)
        ++endingSections
    }

    val relTab = RelTab().also {
        sections.add(sections.size - endingSections, it)
        ++endingSections
    }

    val relaTab = RelaTab().also {
        sections.add(sections.size - endingSections, it)
        ++endingSections
    }

    val text = getOrCreateSection(".text", Shdr.SHT_text).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_text
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_text.toULong()
        }
    }

    val rodata = createNewSection(".rodata", Shdr.SHT_rodata).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_rodata
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_rodata.toULong()
        }
    }

    val data = createNewSection(".data", Shdr.SHT_data).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_data
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_data.toULong()
        }
    }

    val bss = createNewSection(".bss", Shdr.SHT_bss).apply {
        when (val shdr = shdr) {
            is ELF32_Shdr -> shdr.sh_flags = Shdr.SHF_bss
            is ELF64_Shdr -> shdr.sh_flags = Shdr.SHF_bss.toULong()
        }
    }

    //

    var currentSection: Section = text


    // PUBLIC METHODS

    fun build(vararg statements: ASNode.Statement): ByteArray {
        statements.forEach {
            it.execute()
        }

        sections.forEach {
            it.resolveReservations(this)
        }

        return writeELFFile()
    }

    // PRIVATE METHODS

    private fun ASNode.Statement.execute() {
        if (this.label != null) {
            currentSection.addLabel(this.label)
        }

        when (this) {
            is ASNode.Statement.Dir -> {
                this.dir.type.build(this@ELFBuilder, this.dir)
            }

            is ASNode.Statement.Empty -> {}

            is ASNode.Statement.Instr -> {
                instruction.nodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    it.assign(symTab)
                }
                instruction.type.resolve(this@ELFBuilder, instruction)
            }

            is ASNode.Statement.Unresolved -> {}
        }
    }

    private fun writeELFFile(): ByteArray {
        val buffer = ByteBuffer(endianness)

        val phdrSize = phdrs.firstOrNull()?.byteSize()?.toULong() ?: 0U
        val phdrCount = phdrs.size.toULong()

        val ehdr = createELFHeader()

        val totalEhdrBytes = ehdr.byteSize().toULong()
        val totalPhdrBytes = phdrSize * phdrCount
        val totalSectionBytes = sections.sumOf { it.content.size.toULong() }
        val shstrndx = sections.indexOf(shStrTab).toUShort()
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
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()

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
    inner class SymTab : Section {
        val name = ".symtab"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()

        private val symbols: MutableList<Section.SymbolDef> = mutableListOf()
        val symbolSize = Sym.size(ei_class)

        init {
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_SYMTAB
            when (shdr) {
                is ELF32_Shdr -> shdr.sh_entsize = Sym.size(ei_class).toUInt()
                is ELF64_Shdr -> shdr.sh_entsize = Sym.size(ei_class).toULong()
            }
        }

        fun search(identifier: String): Section.SymbolDef? = symbols.firstOrNull { strTab.getStringAt(it.symbol.st_name) == identifier }

        fun update(symbolDef: Section.SymbolDef) {
            val newContent = symbolDef.symbol.build(endianness)
            for (i in 0..<symbolSize.toInt()) {
                content[symbolDef.offset.toInt() + i] = newContent[i]
            }
        }

        fun addUndefinedSymbol(identifier: String): Section.SymbolDef {
            return addSymbol(
                when (ei_class) {
                    E_IDENT.ELFCLASS32 -> ELF32_Sym(
                        strTab.addString(identifier),
                        0U,
                        0U,
                        Sym.ELF_ST_INFO(Sym.STB_LOCAL, Sym.STT_NOTYPE).toUByte(),
                        0U,
                        sections.indexOf(currentSection).toUShort()
                    )

                    E_IDENT.ELFCLASS64 -> ELF64_Sym(
                        strTab.addString(identifier),
                        0U,
                        0U,
                        Sym.ELF_ST_INFO(Sym.STB_LOCAL, Sym.STT_NOTYPE).toUShort(),
                        0U,
                        sections.indexOf(currentSection).toULong()
                    )

                    else -> throw InvalidElfClassException(ei_class)
                }
            )
        }

        fun addSymbol(symbol: Sym): Section.SymbolDef {
            val index = content.size.toUInt()
            val symbolDef = Section.SymbolDef(symbol, index)
            symbols.add(symbolDef)
            content.putAll(symbol.build(endianness))
            return symbolDef
        }
    }

    /**
     * This section holds section names.
     */
    inner class SHStrTab : Section {
        val name = ".shstrtab"
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

        val name = ".strtab"
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

    inner class RelTab : Section {
        val name = ".rel"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
        val rels: MutableSet<Section.RelDef> = mutableSetOf()

        init {
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_REL
            when (shdr) {
                is ELF32_Shdr -> shdr.sh_entsize = Rel.size(ei_class).toUInt()
                is ELF64_Shdr -> shdr.sh_entsize = Rel.size(ei_class).toULong()
            }
        }

        fun addEntry(identifier: String, type: Elf_Word) {
            val sym = symTab.search(identifier) ?: symTab.addUndefinedSymbol(identifier)

            val rel = when (ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    ELF32_Rel(
                        currentSection.content.size.toUInt(),
                        ELF32_Rel.R_INFO(sym.offset, type)
                    )
                }

                E_IDENT.ELFCLASS64 -> {
                    ELF64_Rel(
                        currentSection.content.size.toULong(),
                        ELF64_Rel.R_INFO(sym.offset.toULong(), type.toULong())
                    )
                }

                else -> throw InvalidElfClassException(ei_class)
            }

            rels.add(Section.RelDef(rel, content.size.toUInt()))
            content.putAll(rel.build(endianness))
        }
    }

    inner class RelaTab : Section {
        val name = ".rela"
        override val shdr: Shdr = Shdr.create(ei_class)
        override val content: ByteBuffer = ByteBuffer(endianness)
        override val reservations: MutableList<Section.InstrReservation> = mutableListOf()
        override val labels: MutableSet<Section.LabelDef> = mutableSetOf()
        val relas: MutableSet<Section.RelaDef> = mutableSetOf()

        init {
            shdr.sh_name = shStrTab.addString(name)
            shdr.sh_type = Shdr.SHT_RELA
            when (shdr) {
                is ELF32_Shdr -> shdr.sh_entsize = Rela.size(ei_class).toUInt()
                is ELF64_Shdr -> shdr.sh_entsize = Rela.size(ei_class).toULong()
            }
        }

        fun addEntry(identifier: String, type: Elf_Word, addend: Elf_Sxword) {
            val sym = symTab.search(identifier) ?: symTab.addUndefinedSymbol(identifier)

            val rel = when (ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    ELF32_Rela(
                        currentSection.content.size.toUInt(),
                        ELF32_Rel.R_INFO(sym.offset, type),
                        addend.toInt()
                    )
                }

                E_IDENT.ELFCLASS64 -> {
                    ELF64_Rela(
                        currentSection.content.size.toULong(),
                        ELF64_Rel.R_INFO(sym.offset.toULong(), type.toULong()),
                        addend
                    )
                }

                else -> throw InvalidElfClassException(ei_class)
            }

            relas.add(Section.RelaDef(rel, content.size.toUInt()))
            content.putAll(rel.build(endianness))
        }
    }

    // INTERFACES

    interface Section {
        val shdr: Shdr
        val content: ByteBuffer
        val reservations: MutableList<InstrReservation>
        val labels: MutableSet<LabelDef>

        fun addLabel(label: ASNode.Label) {
            labels.add(LabelDef(label, content.size.toUInt()))
        }

        fun queueLateInit(instr: ASNode.Instruction, byteAmount: Int) {
            reservations.add(InstrReservation(instr, content.size.toUInt()))
            content.putAll(ByteArray(byteAmount) { 0 })
        }

        fun resolveReservations(builder: ELFBuilder) {
            reservations.forEach { def ->
                def.instr.nodes.filterIsInstance<ASNode.NumericExpr>().forEach { expr ->
                    expr.assign(this)
                }
                def.instr.type.lateEvaluation(builder, this, def.instr, def.offset.toInt())
            }
            reservations.clear()
        }

        data class LabelDef(val label: ASNode.Label, val offset: Elf_Word)
        data class InstrReservation(val instr: ASNode.Instruction, val offset: Elf_Word)
        data class SymbolDef(val symbol: Sym, val offset: Elf_Word)
        data class RelDef(val rel: Rel, val offset: Elf_Word)
        data class RelaDef(val rela: Rela, val offset: Elf_Word)
    }

    // EXCEPTIONS

    open class ELFBuilderException(message: String) : Exception(message)


    class InvalidElfClassException(ei_class: Elf_Byte) : ELFBuilderException("Invalid ELF Class $ei_class.")
    class InvalidElfDataException(ei_data: Elf_Byte) : ELFBuilderException("Invalid ELF Data $ei_data type.")
}