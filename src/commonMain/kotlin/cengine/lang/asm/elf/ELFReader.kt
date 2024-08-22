package cengine.lang.asm.elf

import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf32.ELF32_Shdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Shdr
import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * Throws Exception if the file is not matching the ELF Format.
 */
class ELFReader(val fileContent: ByteArray) {
    val e_ident: E_IDENT = E_IDENT.extractFrom(fileContent)
    val ehdr: Ehdr = Ehdr.extractFrom(fileContent, e_ident)
    val endianess: Endianness = when (e_ident.ei_data) {
        E_IDENT.ELFDATA2MSB -> Endianness.BIG
        E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
        else -> throw ELFBuilder.InvalidElfDataException(e_ident.ei_data)
    }
    val buffer = ByteBuffer(endianess, fileContent)

    val sectionHeaders: List<Shdr>
    val programHeaders: List<Phdr>
    val symbolTable: List<Sym>?
    val dynamicSection: List<Dyn>?
    val relocationTables: Map<String, List<Rel>>
    val relocationTablesWithAddend: Map<String, List<Rela>>
    val noteHeaders: List<Nhdr>?

    init {
        sectionHeaders = readSectionHeaders()

        programHeaders = readProgramHeaders()

        symbolTable = readSymbolTable()

        dynamicSection = readDynamicSection()

        relocationTables = readRelocationTables()

        relocationTablesWithAddend = readRelocationTablesWithAddend()

        noteHeaders = readNoteHeaders()
    }

    private fun readSectionHeaders(): List<Shdr> {
        return when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> {
                val offset = (ehdr as ELF32_Ehdr).e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    Shdr.extractFrom(fileContent, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }

            E_IDENT.ELFCLASS64 -> {
                val offset = (ehdr as ELF64_Ehdr).e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    Shdr.extractFrom(fileContent, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }

            else -> emptyList()
        }
    }

    private fun readProgramHeaders(): List<Phdr> {
        return when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> {
                val offset = (ehdr as ELF32_Ehdr).e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    Phdr.extractFrom(fileContent, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }

            E_IDENT.ELFCLASS64 -> {
                val offset = (ehdr as ELF64_Ehdr).e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    Phdr.extractFrom(fileContent, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }

            else -> emptyList()
        }
    }

    private fun readSymbolTable(): List<Sym>? {
        val symtabSection = sectionHeaders.find { it.sh_type == Shdr.SHT_SYMTAB }
        return symtabSection?.let {
            when (e_ident.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val offset = (it as ELF32_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Sym.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                E_IDENT.ELFCLASS64 -> {
                    val offset = (it as ELF64_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Sym.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }
        }
    }

    private fun readDynamicSection(): List<Dyn>? {
        val dynamicSection = sectionHeaders.find { it.sh_type == Shdr.SHT_DYNAMIC }
        return dynamicSection?.let {
            when (e_ident.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val offset = (it as ELF32_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Dyn.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                E_IDENT.ELFCLASS64 -> {
                    val offset = (it as ELF64_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Dyn.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }

        }
    }

    private fun readRelocationTables(): Map<String, List<Rel>> {
        return when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> {
                sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
                    .associate { section ->
                        val sectionName = getSectionName(section)
                        val offset = (section as ELF32_Shdr).sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            Rel.extractFrom(fileContent, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }
            }

            E_IDENT.ELFCLASS64 -> {
                sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
                    .associate { section ->
                        val sectionName = getSectionName(section)
                        val offset = (section as ELF64_Shdr).sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            Rel.extractFrom(fileContent, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }
            }

            else -> emptyMap<String, List<Rel>>()
        }
    }

    private fun readRelocationTablesWithAddend(): Map<String, List<Rela>> {
        return when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> {
                sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
                    .associate { section ->
                        val sectionName = getSectionName(section)
                        val offset = (section as ELF32_Shdr).sh_offset.toInt()
                        val relaEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            Rela.extractFrom(fileContent, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relaEntries
                    }
            }

            E_IDENT.ELFCLASS64 -> {
                sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
                    .associate { section ->
                        val sectionName = getSectionName(section)
                        val offset = (section as ELF64_Shdr).sh_offset.toInt()
                        val relaEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            Rela.extractFrom(fileContent, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relaEntries
                    }
            }

            else -> emptyMap<String, List<Rela>>()
        }
    }

    private fun readNoteHeaders(): List<Nhdr>? {
        val noteSection = sectionHeaders.find { it.sh_type == Shdr.SHT_NOTE }
        return noteSection?.let {
            when (e_ident.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val offset = (it as ELF32_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Nhdr.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                E_IDENT.ELFCLASS64 -> {
                    val offset = (it as ELF64_Shdr).sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        Nhdr.extractFrom(fileContent, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                else -> null
            }
        }
    }

    fun getSectionName(section: Shdr): String {
        val stringTableSection = sectionHeaders[ehdr.e_shstrndx.toInt()]
        when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> {
                val stringTableOffset = (stringTableSection as ELF32_Shdr).sh_offset.toInt()
                val nameOffset = section.sh_name.toInt()
                return fileContent.sliceArray(stringTableOffset + nameOffset until fileContent.size)
                    .takeWhile { it != 0.toByte() }
                    .toByteArray()
                    .decodeToString()
            }

            E_IDENT.ELFCLASS64 -> {
                val stringTableOffset = (stringTableSection as ELF64_Shdr).sh_offset.toInt()
                val nameOffset = section.sh_name.toInt()
                return fileContent.sliceArray(stringTableOffset + nameOffset until fileContent.size)
                    .takeWhile { it != 0.toByte() }
                    .toByteArray()
                    .decodeToString()
            }
        }
        return ""
    }


}