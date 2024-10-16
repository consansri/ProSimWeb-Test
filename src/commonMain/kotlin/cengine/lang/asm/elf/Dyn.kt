package cengine.lang.asm.elf

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
sealed interface Dyn : BinaryProvider {

    companion object {
        fun getDynamicType(tag: Elf_Sxword): String = when (tag) {
            DT_NULL -> "NULL"
            DT_NEEDED -> "NEEDED"
            DT_PLTRELSZ -> "PLTRELSZ"
            DT_PLTGOT -> "PLTGOT"
            DT_HASH -> "HASH"
            DT_STRTAB -> "STRTAB"
            DT_SYMTAB -> "SYMTAB"
            DT_RELA -> "RELA"
            DT_RELASZ -> "RELASZ"
            DT_RELAENT -> "RELAENT"
            DT_STRSZ -> "STRSZ"
            DT_SYMENT -> "SYMENT"
            DT_INIT -> "INIT"
            DT_FINI -> "FINI"
            DT_SONAME -> "SONAME"
            DT_RPATH -> "RPATH"
            DT_SYMBOLIC -> "SYMBOLIC"
            DT_REL -> "REL"
            DT_RELSZ -> "RELSZ"
            DT_RELENT -> "RELENT"
            DT_PLTREL -> "PLTREL"
            DT_DEBUG -> "DEBUG"
            DT_TEXTREL -> "TEXTREL"
            DT_JMPREL -> "JMPREL"
            DT_BIND_NOW -> "BIND_NOW"
            else -> "UNKNOWN (${tag})"
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Dyn {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val d_tag = byteArray.loadInt(eIdent, currIndex)
                    currIndex += 4
                    val d_un = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val d_ptr = byteArray.loadUInt(eIdent, currIndex)

                    return ELF32_Dyn(d_tag, d_un, d_ptr)
                }

                E_IDENT.ELFCLASS64 -> {
                    val d_tag = byteArray.loadLong(eIdent, currIndex)
                    currIndex += 8
                    val d_un = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val d_ptr = byteArray.loadULong(eIdent, currIndex)
                    return ELF64_Dyn(d_tag, d_un, d_ptr)
                }

                else -> throw NotInELFFormatException
            }
        }


        /**
         * Marks end of dynamic section.
         */
        const val DT_NULL = 0L

        /**
         * String table offset to name of a needed library.
         */
        const val DT_NEEDED = 1L

        /**
         * Size in bytes of PLT relocation entries.
         */
        const val DT_PLTRELSZ = 2L

        /**
         * Address of PLT and/or GOT.
         */
        const val DT_PLTGOT = 3L

        /**
         * Address of symbol hash table.
         */
        const val DT_HASH = 4L

        /**
         * Address of string table.
         */
        const val DT_STRTAB = 5L

        /**
         * Address of symbol table.
         */
        const val DT_SYMTAB = 6L

        /**
         * Address of Rela relocation table.
         */
        const val DT_RELA = 7L

        /**
         * Size in bytes of the Rela relocation table.
         */
        const val DT_RELASZ = 8L

        /**
         * Size in bytes of a Rela relocation table entry.
         */
        const val DT_RELAENT = 9L

        /**
         * Size in bytes of string table.
         */
        const val DT_STRSZ = 10L

        /**
         * Size in bytes of a symbol table entry.
         */
        const val DT_SYMENT = 11L

        /**
         * Address of the initialization function.
         */
        const val DT_INIT = 12L

        /**
         * Address of the termination function.
         */
        const val DT_FINI = 13L

        /**
         * String table offset to name of shared object.
         */
        const val DT_SONAME = 14L

        /**
         * String table offset to search path for direct and indirect library dependencies.
         */
        const val DT_RPATH = 15L

        /**
         * Alert linker to search this shared object before the executable for symbols.
         */
        const val DT_SYMBOLIC = 16L

        /**
         * Address of Rel relocation table.
         */
        const val DT_REL = 17L

        /**
         * Size in bytes of Rel relocation table.
         */
        const val DT_RELSZ = 18L

        /**
         * Size in bytes of a Rel table entry.
         */
        const val DT_RELENT = 19L

        /**
         * Type of relocation entry to which the PLT refers (Rela or Rel).
         */
        const val DT_PLTREL = 20L

        /**
         * Undefined use for debugging.
         */
        const val DT_DEBUG = 21L

        /**
         * Absence of this entry indicates that no relocation entries should apply to a nonwritable segment.
         */
        const val DT_TEXTREL = 22L

        /**
         * Address of relocation entries associated solely with the PLT.
         */
        const val DT_JMPREL = 23L

        /**
         * Instruct dynamic linker to process all relocations before transferring control to the executable.
         */
        const val DT_BIND_NOW = 24L

        /**
         * String table offset to search path for direct library dependencies.
         */
        const val DT_RUNPATH = 25L

        /**
         * Lower bound of processor-specific semantic values.
         */
        const val DT_LOPROC = 0x70000000L

        /**
         * Upper bound of processor-specific semantic values.
         */
        const val DT_HIPROC = 0x7fffffffL
    }

}