package cengine.lang.asm.elf32

/**
 * ELF Section Header
 *
 * @param sh_name This member specifies the name of the section. Its value is an index into
 * the section header string table section [see "String Table'' below], giving
 * the location of a null-terminated string.
 *
 * @param sh_type This member categorizes the section's contents and semantics.
 *
 * @param sh_flags Sections support 1-bit flags that describe miscellaneous attributes.
 *
 * @param sh_addr If the section will appear in the memory image of a process, this member
 * gives the address at which the section's first byte should reside. Otherwise,
 * the member contains 0.
 *
 * @param sh_offset This member's value gives the byte offset from the beginning of the file to
 * the first byte in the section. One section type, SHT_NOBITS described
 * below, occupies no space in the file, and its sh_offset member locates
 * the conceptual placement in the file.
 *
 * @param sh_size This member gives the section's size in bytes.  Unless the section type is
 * SHT_NOBITS, the section occupies sh_size bytes in the file. A section
 * of type SHT_NOBITS may have a non-zero size, but it occupies no space
 * in the file.
 *
 * @param sh_link This member holds a section header table index link, whose interpretation
 * depends on the section type. If not defined/used then its default value is [SHN_UNDEF].
 *
 * @param sh_info This member holds extra information, whose interpretation depends on the
 * section type. If not defined/used then its default value is [0U].
 *
 * @param sh_addralign Some sections have address alignment constraints. For example, if a section
 * holds a doubleword, the system must ensure doubleword alignment for the
 * entire section.  That is, the value of sh_addr must be congruent to 0,
 * modulo the value of sh_addralign. Currently, only 0 and positive
 * integral powers of two are allowed. Values 0 and 1 mean the section has no
 * alignment constraints.
 *
 * @param sh_entsize Some sections hold a table of fixed-size entries, such as a symbol table. For
 * such a section, this member gives the size in bytes of each entry. The
 * member contains 0 if the section does not hold a table of fixed-size entries.
 *
 *
 *
 */
data class ELF32_Shdr(
    var sh_name: ELF32_WORD = 0U,
    var sh_type: ELF32_WORD = SHT_NULL,
    var sh_flags: ELF32_WORD = 0U,
    var sh_addr: ELF32_ADDR = 0U,
    var sh_offset: ELF32_OFF = 0U,
    var sh_size: ELF32_WORD = 0U,
    var sh_link: ELF32_WORD = SHN_UNDEF,
    var sh_info: ELF32_WORD = 0U,
    var sh_addralign: ELF32_WORD = 0U,
    var sh_entsize: ELF32_WORD = 0U
): BinaryProvider {

    companion object {

        /**
         * Special Section Indexes
         */

        /**
         * This value marks an undefined, missing, irrelevant, or otherwise
         * meaningless section reference. For example, a symbol "defined'' relative to
         * section number [SHN_UNDEF] is an undefined symbol.
         */
        const val SHN_UNDEF: ELF32_WORD = 0U

        /**
         * This value specifies the lower bound of the range of reserved indexes.
         */
        const val SHN_LORESERVE: ELF32_WORD = 0xff00U

        /**
         * [SHN_LOPROC] .. [SHN_HIPROC] Values in this inclusive range are reserved for processor-specific  semantics.
         */
        const val SHN_LOPROC: ELF32_WORD = 0xff00U

        /**
         * [SHN_LOPROC] .. [SHN_HIPROC] Values in this inclusive range are reserved for processor-specific  semantics.
         */
        const val SHN_HIPROC: ELF32_WORD = 0xff1fU

        /**
         *  This value specifies absolute values for the corresponding reference. For
         * example, symbols defined relative to section number [SHN_ABS] have
         * absolute values and are not affected by relocation.
         */
        const val SHN_ABS: ELF32_WORD = 0xfff1U

        /**
         * Symbols defined relative to this section are common symbols, such as
         * FORTRAN COMMON or unallocated C external variables.
         */
        const val SHN_COMMON: ELF32_WORD = 0xfff2U

        /**
         * This value specifies the upper bound of the range of reserved indexes. The
         * system reserves indexes between [SHN_LORESERVE] and
         * [SHN_HIRESERVE], inclusive; the values do not reference the section header
         * table. That is, the section header table does not contain entries for the
         * reserved indexes.
         */
        const val SHN_HIRESERVE: ELF32_WORD = 0xffffU

        /**
         * [sh_type]
         */

        /**
         * This value marks the section header as inactive; it does not have an
         * associated section. Other members of the section header have undefined
         * values.
         */
        val SHT_NULL: ELF32_WORD = 0U

        /**
         * The section holds information defined by the program, whose format and
         * meaning are determined solely by the program.
         */
        val SHT_PROGBITS: ELF32_WORD = 1U

        /**
         * These sections hold a symbol table.
         *
         * - [sh_link] : This information is operating system specific.
         * - [sh_info] : This information is operating system specific.
         */
        val SHT_SYMTAB: ELF32_WORD = 2U

        /**
         * The section holds a string table.
         */
        val SHT_STRTAB: ELF32_WORD = 3U

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
        val SHT_RELA: ELF32_WORD = 4U

        /**
         * The section holds a symbol hash table.
         *
         * - [sh_link] : The section header index
         * of the symbol table to
         * which the hash table
         * applies.
         * - [sh_info] : 0
         */
        val SHT_HASH: ELF32_WORD = 5U

        /**
         * The section holds information for dynamic linking.
         *
         * - [sh_link] : The section header index
         * of the string table used by
         * entries in the section.
         * - [sh_info] : 0
         */
        val SHT_DYNAMIC: ELF32_WORD = 6U

        /**
         * This section holds information that marks the file in some way.
         */
        val SHT_NOTE: ELF32_WORD = 7U

        /**
         * A section of this type occupies no space in the file but otherwise resembles
         * SHT_PROGBITS. Although this section contains no bytes, the
         * sh_offset member contains the conceptual file offset.
         */
        val SHT_NOBITS: ELF32_WORD = 8U

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
        val SHT_REL: ELF32_WORD = 9U

        /**
         * This section type is reserved but has unspecified semantics.
         */
        val SHT_SHLIB: ELF32_WORD = 10U

        /**
         * These sections hold a symbol table.
         *
         * - [sh_link] : This information is operating system specific.
         * - [sh_info] : This information is operating system specific.
         */
        val SHT_DYNSYM: ELF32_WORD = 11U

        /**
         * Values in this inclusive range are reserved for processor-specific semantics.
         */
        val SHT_LOPROC: ELF32_WORD = 0x70000000U

        /**
         * Values in this inclusive range are reserved for processor-specific semantics.
         */
        val SHT_HIPROC: ELF32_WORD = 0x7fffffffU

        /**
         * This value specifies the lower bound of the range of indexes reserved for
         * application programs.
         */
        val SHT_LOUSER: ELF32_WORD = 0x80000000U

        /**
         * This value specifies the upper bound of the range of indexes reserved for
         * application programs. Section types between SHT_LOUSER and
         * SHT_HIUSER may be used by the application, without conflicting with
         * current or future system-defined section types.
         */
        val SHT_HIUSER: ELF32_WORD = 0xFFFFFFFFU

        /**
         * [sh_flags]
         */

        /**
         * The section contains data that should be writable during process execution.
         */
        const val SHF_WRITE: ELF32_WORD = 0x1U

        /**
         * The section occupies memory during process execution. Some control
         * sections do not reside in the memory image of an object file; this attribute
         * is off for those sections.
         */
        const val SHF_ALLOC: ELF32_WORD = 0x2U

        /**
         * The section contains executable machine instructions.
         */
        const val SHF_EXECINSTR: ELF32_WORD = 0x4U

        /**
         * All bits included in this mask are reserved for processor-specific semantics.
         */
        const val SHF_MASKPROC: ELF32_WORD = 0xf0000000U

        /**
         * Special Sections
         */

        /**
         * This section holds uninitialized data that contribute to the program's
         * memory image. By definition, the system initializes the data with zeros
         * when the program begins to run. The section occupies no file space, as
         * indicated by the section type, [SHT_NOBITS].
         */
        fun createBSS(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_NOBITS,
            sh_flags = SHF_ALLOC + SHF_WRITE
        )

        /**
         * This section holds version control information.
         */
        fun createCOMMENT(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS
        )

        /**
         * ([createDATA] and [createDATA1]) These sections hold initialized data that contribute to the program's memory
         * image.
         */
        fun createDATA(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS,
            sh_flags = SHF_ALLOC + SHF_WRITE
        )

        /**
         * ([createDATA] and [createDATA1]) These sections hold initialized data that contribute to the program's memory
         * image.
         */
        fun createDATA1(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS,
            sh_flags = SHF_ALLOC + SHF_WRITE
        )

        /**
         * This section holds information for symbolic debugging. The contents are
         * unspecified.  All section names with the prefix .debug are reserved for
         * future use.
         */
        fun createDEBUG(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS
        )

        /**
         * This section holds dynamic linking information and has attributes such as
         * [SHF_ALLOC] and [SHF_WRITE]. Whether the [SHF_WRITE] bit is set is
         * determined by the operating system and processor.
         */
        fun createDYNAMIC(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_DYNAMIC
        )

        /**
         * This section holds a symbol hash table.
         */
        fun createHASH(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_HASH,
            sh_flags = SHF_ALLOC
        )

        /**
         * This section holds line number information for symbolic debugging, which
         * describes the correspondence between the source program and the machine
         * code. The contents are unspecified.
         */
        fun createLINE(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS
        )

        /**
         * This section holds information in the format that is described in the "Note
         * Section'' in Chapter 2.
         */
        fun createNOTE(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_NOTE
        )

        /**
         * These sections hold read-only data that typically contribute to a
         * non-writable segment in the process image. See "Program Header'' in
         * Chapter 2 for more information.
         */
        fun createRODATA(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS,
            sh_flags = SHF_ALLOC
        )

        /**
         * These sections hold read-only data that typically contribute to a
         * non-writable segment in the process image. See "Program Header'' in
         * Chapter 2 for more information.
         */
        fun createRODATA1(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS,
            sh_flags = SHF_ALLOC
        )

        /**
         * This section holds section names.
         */
        fun createSHSTRTAB(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_STRTAB
        )

        /**
         * This section holds strings, most commonly the strings that represent the names
         * associated with symbol table entries. If a file has a loadable segment that
         * includes the symbol string table, the section's attributes will include the
         * [SHF_ALLOC] bit; otherwise, that bit will be off.
         */
        fun createSTRTAB(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_STRTAB
        )

        /**
         * This section holds a symbol table, as "Symbol Table'' in this chapter
         * describes. If a file has a loadable segment that includes the symbol table,
         * the section's attributes will include the [SHF_ALLOC] bit; otherwise, that bit
         * will be off.
         */
        fun createSYMTAB(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_SYMTAB
        )

        /**
         *  This section holds the "text,'' or executable instructions, of a program.
         */
        fun createTEXT(): ELF32_Shdr = ELF32_Shdr(
            sh_type = SHT_PROGBITS,
            sh_flags = SHF_ALLOC + SHF_EXECINSTR
        )


    }

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }

}