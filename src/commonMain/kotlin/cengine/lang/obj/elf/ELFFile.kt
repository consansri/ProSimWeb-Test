package cengine.lang.obj.elf

sealed class ELFFile<EHDR : Ehdr, SHDR : Shdr, PHDR : Phdr, SYM : Sym, DYN : Dyn, REL : Rel, RELA : Rela>(val name: String, val content: ByteArray) {

    companion object {
        fun parse(name: String, content: ByteArray): ELFFile<*, *, *, *, *, *, *>? {
            if (!name.endsWith(".o")) return null

            try {
                return ELF32File(name, content)
            } catch (e: Exception) {

            }

            try {
                return ELF64File(name, content)
            } catch (e: Exception) {

            }

            return null
        }
    }

    val e_ident: E_IDENT = eIdent(content)
    val ehdr: EHDR = ehdr(content, e_ident)

    val sectionHeaders: List<SHDR> = readSectionHeaders()
    val programHeaders: List<PHDR> = readProgramHeaders()
    val symbolTable: List<SYM>? = readSymbolTable()
    val dynamicSection: List<DYN>? = readDynamicSection()
    val relocationTables: Map<String, List<REL>> = readRelocationTables()
    val relocationTablesWithAddend: Map<String, List<RELA>> = readRelocationTablesWithAddend()
    val noteHeaders: List<Nhdr>? = readNoteHeaders()
    val segmentToSectionGroup: List<Group> = groupSectionsBySegment()
    val shstrtab: SHDR? = sectionHeaders.getOrNull(ehdr.e_shstrndx.toInt())
    val strTab = sectionHeaders.firstOrNull { getSectionName(it) == ".strtab" }

    private fun readSectionHeaders(): List<SHDR> {
        return when (ehdr) {
            is ELF32_Ehdr -> {
                val offset = ehdr.e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    shdr(content, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }

            is ELF64_Ehdr -> {
                val offset = (ehdr as ELF64_Ehdr).e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    shdr(content, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }

            else -> emptyList()
        }
    }

    private fun readProgramHeaders(): List<PHDR> {
        return when (ehdr) {
            is ELF32_Ehdr -> {
                val offset = ehdr.e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    phdr(content, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }

            is ELF64_Ehdr -> {
                val offset = (ehdr as ELF64_Ehdr).e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    phdr(content, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }

            else -> emptyList()
        }
    }

    private fun readSymbolTable(): List<SYM>? {
        val symtabSection = sectionHeaders.find { it.sh_type == Shdr.SHT_SYMTAB }
        return symtabSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        sym(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        sym(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }
        }
    }

    private fun readDynamicSection(): List<DYN>? {
        val dynamicSection = sectionHeaders.find { it.sh_type == Shdr.SHT_DYNAMIC }
        return dynamicSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = (it as ELF32_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        dyn(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = (it as ELF64_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        dyn(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }

        }
    }

    private fun readRelocationTables(): Map<String, List<REL>> {
        return sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
            .associate { section ->
                val sectionName = getSectionName(section)
                when (section) {
                    is ELF32_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rel(content, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    is ELF64_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rel(content, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    else -> throw Exception("Invalid Shdr type: ${section::class.simpleName}")
                }
            }

    }

    private fun readRelocationTablesWithAddend(): Map<String, List<RELA>> {
        return sectionHeaders.filter { it.sh_type == Shdr.SHT_RELA }
            .associate { section ->
                val sectionName = getSectionName(section)
                when (section) {
                    is ELF32_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rela(content, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    is ELF64_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rela(content, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    else -> throw Exception("Invalid Shdr type: ${section::class.simpleName}")
                }
            }

    }

    private fun readNoteHeaders(): List<Nhdr>? {
        val noteSection = sectionHeaders.find { it.sh_type == Shdr.SHT_NOTE }
        return noteSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        nhdr(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        nhdr(content, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }
        }
    }

    abstract fun getSectionName(section: SHDR): String

    abstract fun getStrTabString(namendx: Int): String?

    private fun groupSectionsBySegment(): List<Group> {
        val segments = programHeaders.map { phdr ->
            val start = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toULong()
                is ELF64_Phdr -> phdr.p_offset
                else -> throw ELFGenerator.InvalidElfClassException(e_ident.ei_class)
            }
            val size = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toULong()
                is ELF64_Phdr -> phdr.p_filesz
                else -> throw ELFGenerator.InvalidElfClassException(e_ident.ei_class)
            }

            val fileIndexRange = start..<(start + size)

            val sections = sectionHeaders.filter { shdr ->
                when (shdr) {
                    is ELF32_Shdr -> shdr.sh_offset in fileIndexRange
                    is ELF64_Shdr -> shdr.sh_offset in fileIndexRange
                    else -> throw ELFGenerator.InvalidElfClassException(e_ident.ei_class)
                }

            }

            createSegment(phdr, sections)
        }
        val unmatched = (sectionHeaders - segments.flatMap { it.sections }.toSet()).map {
            createSection(it)
        }
        return (segments + unmatched).sortedBy { it.index }
    }

    fun nameOfSection(index: Int): String {
        val section = sectionHeaders.getOrNull(index) ?: return ""
        return getSectionName(section)
    }


    abstract fun createSegment(phdr: PHDR, sections: List<SHDR>): Segment
    abstract fun createSection(shdr: SHDR): Section

    sealed class Group(val index: Int)
    abstract inner class Segment(val phdr: PHDR, val sections: List<SHDR>) : Group(sectionHeaders.indexOf(sections.firstOrNull()))
    abstract inner class Section(val section: SHDR) : Group(sectionHeaders.indexOf(section))

    fun eIdent(byteArray: ByteArray): E_IDENT = E_IDENT.extractFrom(byteArray)

    abstract fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): EHDR

    abstract fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): SHDR

    abstract fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): PHDR

    abstract fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): SYM

    abstract fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): DYN

    abstract fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): REL

    abstract fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): RELA

    fun nhdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Nhdr = Nhdr.extractFrom(byteArray, eIdent, offset)

}