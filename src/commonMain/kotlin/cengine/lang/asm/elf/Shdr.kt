package cengine.lang.asm.elf

import cengine.lang.asm.elf.elf32.ELF32_Shdr
import cengine.lang.asm.elf.elf64.ELF64_Shdr

interface Shdr : BinaryProvider {

    var sh_name: Elf_Word
    var sh_type: Elf_Word
    var sh_link: Elf_Word
    var sh_info: Elf_Word

    companion object {

        fun size(ei_class: Elf_Byte): Elf_Half{
            return when(ei_class){
                E_IDENT.ELFCLASS32 -> 40U
                E_IDENT.ELFCLASS64 -> 64U
                else -> throw ELFBuilder.InvalidElfClassException(ei_class)
            }
        }

        fun create(ei_class: Elf_Byte): Shdr = when (ei_class) {
            E_IDENT.ELFCLASS32 -> ELF32_Shdr()
            E_IDENT.ELFCLASS64 -> ELF64_Shdr()
            else -> throw ELFBuilder.InvalidElfClassException(ei_class)
        }

        fun getSectionType(type: Elf_Word): String = when (type) {
            SHT_NULL -> "NULL"
            SHT_PROGBITS -> "PROGBITS"
            SHT_SYMTAB -> "SYMTAB"
            SHT_STRTAB -> "STRTAB"
            SHT_RELA -> "RELA"
            SHT_HASH -> "HASH"
            SHT_DYNAMIC -> "DYNAMIC"
            SHT_NOTE -> "NOTE"
            SHT_NOBITS -> "NOBITS"
            SHT_REL -> "REL"
            SHT_SHLIB -> "SHLIB"
            SHT_DYNSYM -> "DYNSYM"
            else -> "UNKNOWN (0x${type.toString(16)})"
        }

        fun getSectionFlags(flags: Elf_Xword): String {
            val flagsList = mutableListOf<String>()
            if (flags and SHF_WRITE.toULong() != 0UL) flagsList.add("W")
            if (flags and SHF_ALLOC.toULong() != 0UL) flagsList.add("A")
            if (flags and SHF_EXECINSTR.toULong() != 0UL) flagsList.add("X")
            return flagsList.joinToString("")
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Shdr {
            var currIndex = offset
            val sh_name = byteArray.loadUInt(eIdent, currIndex)
            currIndex += 4
            val sh_type = byteArray.loadUInt(eIdent, currIndex)
            currIndex += 4

            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val sh_flags = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_addr = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_offset = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_size = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_link = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_info = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_addralign = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_entsize = byteArray.loadUInt(eIdent, currIndex)

                    return ELF32_Shdr(sh_name, sh_type, sh_flags, sh_addr, sh_offset, sh_size, sh_link, sh_info, sh_addralign, sh_entsize)
                }

                E_IDENT.ELFCLASS64 -> {
                    val sh_flags = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val sh_addr = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val sh_offset = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val sh_size = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val sh_link = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_info = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val sh_addralign = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val sh_entsize = byteArray.loadULong(eIdent, currIndex)

                    return ELF64_Shdr(sh_name, sh_type, sh_flags, sh_addr, sh_offset, sh_size, sh_link, sh_info, sh_addralign, sh_entsize)
                }

                else -> throw NotInELFFormatException
            }
        }

        /**
         * Special Section Indexes
         */

        /**
         * This value marks an undefined, missing, irrelevant, or otherwise
         * meaningless section reference. For example, a symbol "defined'' relative to
         * section number [SHN_UNDEF] is an undefined symbol.
         */
        const val SHN_UNDEF: Elf_Half = 0U

        /**
         * This value specifies the lower bound of the range of reserved indexes.
         */
        const val SHN_LORESERVE: Elf_Half = 0xff00U

        /**
         * [SHN_LOPROC] .. [SHN_HIPROC] Values in this inclusive range are reserved for processor-specific  semantics.
         */
        const val SHN_LOPROC: Elf_Half = 0xff00U

        /**
         * [SHN_LOPROC] .. [SHN_HIPROC] Values in this inclusive range are reserved for processor-specific  semantics.
         */
        const val SHN_HIPROC: Elf_Half = 0xff1fU

        /**
         *  This value specifies absolute values for the corresponding reference. For
         * example, symbols defined relative to section number [SHN_ABS] have
         * absolute values and are not affected by relocation.
         */
        const val SHN_ABS: Elf_Half = 0xfff1U

        /**
         * Symbols defined relative to this section are common symbols, such as
         * FORTRAN COMMON or unallocated C external variables.
         */
        const val SHN_COMMON: Elf_Half = 0xfff2U

        /**
         * This value specifies the upper bound of the range of reserved indexes. The
         * system reserves indexes between [SHN_LORESERVE] and
         * [SHN_HIRESERVE], inclusive; the values do not reference the section header
         * table. That is, the section header table does not contain entries for the
         * reserved indexes.
         */
        const val SHN_HIRESERVE: Elf_Word = 0xffffU

        /**
         * [sh_type]
         */

        /**
         * This value marks the section header as inactive; it does not have an
         * associated section. Other members of the section header have undefined
         * values.
         */
        val SHT_NULL: Elf_Word = 0U

        /**
         * The section holds information defined by the program, whose format and
         * meaning are determined solely by the program.
         */
        val SHT_PROGBITS: Elf_Word = 1U

        /**
         * These sections hold a symbol table.
         *
         * - [sh_link] : This information is operating system specific.
         * - [sh_info] : This information is operating system specific.
         */
        val SHT_SYMTAB: Elf_Word = 2U

        /**
         * The section holds a string table.
         */
        val SHT_STRTAB: Elf_Word = 3U

        /**
         * The section holds relocation entries with explicit addends, such as type
         * Elf32_Rela for the 32-bit class of object files. An object file may have
         * multiple relocation sections. See "Relocation'' below for details.
         *
         * - [sh_link] : The section header index
         * of the associated symbol
         * table.
         * - [sh_info] : The section header index
         * of the section to which the
         * relocation applies.
         */
        val SHT_RELA: Elf_Word = 4U

        /**
         * The section holds a symbol hash table.
         *
         * - [sh_link] : The section header index
         * of the symbol table to
         * which the hash table
         * applies.
         * - [sh_info] : 0
         */
        val SHT_HASH: Elf_Word = 5U

        /**
         * The section holds information for dynamic linking.
         *
         * - [sh_link] : The section header index
         * of the string table used by
         * entries in the section.
         * - [sh_info] : 0
         */
        val SHT_DYNAMIC: Elf_Word = 6U

        /**
         * This section holds information that marks the file in some way.
         */
        val SHT_NOTE: Elf_Word = 7U

        /**
         * A section of this type occupies no space in the file but otherwise resembles
         * SHT_PROGBITS. Although this section contains no bytes, the
         * sh_offset member contains the conceptual file offset.
         */
        val SHT_NOBITS: Elf_Word = 8U

        /**
         * The section holds relocation entries without explicit addends, such as type
         * Elf32_Rel for the 32-bit class of object files. An object file may have
         * multiple relocation sections. See "Relocation'' below for details.
         *
         * - [sh_link] : The section header index
         * of the associated symbol
         * table.
         * - [sh_info] : The section header index
         * of the section to which the
         * relocation applies.
         *
         */
        val SHT_REL: Elf_Word = 9U

        /**
         * This section type is reserved but has unspecified semantics.
         */
        val SHT_SHLIB: Elf_Word = 10U

        /**
         * These sections hold a symbol table.
         *
         * - [sh_link] : This information is operating system specific.
         * - [sh_info] : This information is operating system specific.
         */
        val SHT_DYNSYM: Elf_Word = 11U

        /**
         * Values in this inclusive range are reserved for processor-specific semantics.
         */
        val SHT_LOPROC: Elf_Word = 0x70000000U

        /**
         * Values in this inclusive range are reserved for processor-specific semantics.
         */
        val SHT_HIPROC: Elf_Word = 0x7fffffffU

        /**
         * This value specifies the lower bound of the range of indexes reserved for
         * application programs.
         */
        val SHT_LOUSER: Elf_Word = 0x80000000U

        /**
         * This value specifies the upper bound of the range of indexes reserved for
         * application programs. Section types between SHT_LOUSER and
         * SHT_HIUSER may be used by the application, without conflicting with
         * current or future system-defined section types.
         */
        val SHT_HIUSER: Elf_Word = 0xFFFFFFFFU

        /**
         * [sh_flags]
         */

        /**
         * The section contains data that should be writable during process execution.
         */
        const val SHF_WRITE: Elf_Word = 0x1U

        /**
         * The section occupies memory during process execution. Some control
         * sections do not reside in the memory image of an object file; this attribute
         * is off for those sections.
         */
        const val SHF_ALLOC: Elf_Word = 0x2U

        /**
         * The section contains executable machine instructions.
         */
        const val SHF_EXECINSTR: Elf_Word = 0x4U

        /**
         * All bits included in this mask are reserved for processor-specific semantics.
         */
        const val SHF_MASKPROC: Elf_Word = 0xf0000000U

        /**
         * Special Sections
         */

        /**
         * (BSS) This section holds uninitialized data that contribute to the program's
         * memory image. By definition, the system initializes the data with zeros
         * when the program begins to run. The section occupies no file space, as
         * indicated by the section type, [SHT_NOBITS].
         */
        val SHT_bss = SHT_NOBITS
        val SHF_bss = SHF_ALLOC + SHF_WRITE

        /**
         * (COMMENT) This section holds version control information.
         */
        val SHT_comment = SHT_PROGBITS

        /**
         * (DATA) These sections hold initialized data that contribute to the program's memory
         * image.
         */
        val SHT_data = SHT_PROGBITS
        val SHF_data = SHF_ALLOC + SHF_WRITE

        /**
         * (DEBUG) This section holds information for symbolic debugging. The contents are
         * unspecified.  All section names with the prefix .debug are reserved for
         * future use.
         */
        val SHT_debug = SHT_PROGBITS


        /**
         * (DYNAMIC) This section holds dynamic linking information and has attributes such as
         * [SHF_ALLOC] and [SHF_WRITE]. Whether the [SHF_WRITE] bit is set is
         * determined by the operating system and processor.
         */
        val SHT_dynamic = SHT_DYNAMIC

        /**
         * (HASH) This section holds a symbol hash table.
         */
        val SHT_hash = SHT_HASH
        val SHF_hash = SHF_ALLOC

        /**
         * This section holds line number information for symbolic debugging, which
         * describes the correspondence between the source program and the machine
         * code. The contents are unspecified.
         */
        val SHT_line = SHT_PROGBITS

        /**
         * This section holds information in the format that is described in the "Note
         * Section'' in Chapter 2.
         */

        val SHT_note = SHT_NOTE

        /**
         * These sections hold read-only data that typically contribute to a
         * non-writable segment in the process image. See "Program Header'' in
         * Chapter 2 for more information.
         */

        val SHT_rodata = SHT_PROGBITS
        val SHF_rodata = SHF_ALLOC


        /**
         *  This section holds the "text,'' or executable instructions, of a program.
         */

        val SHT_text = SHT_PROGBITS
        val SHF_text = SHF_ALLOC + SHF_EXECINSTR
    }

    override fun toString(): String
}