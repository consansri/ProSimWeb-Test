package cengine.lang.obj.elf

sealed class Sym : BinaryProvider {

    abstract var st_name: Elf_Word
    abstract var st_info: Elf_Byte
    abstract var st_other: Elf_Byte
    abstract var st_shndx: Elf_Half

    fun setValue(value: Elf_Xword) {
        when (this) {
            is ELF32_Sym -> st_value = value.toUInt()
            is ELF64_Sym -> st_value = value
        }
    }

    fun setSize(size: Elf_Xword) {
        when (this) {
            is ELF32_Sym -> st_size = size.toUInt()
            is ELF64_Sym -> st_size = size
        }
    }

    companion object {

        fun createEmpty(ei_class: Elf_Byte, st_name: Elf_Word, shndx: Elf_Half, type: Elf_Byte = STT_NOTYPE, binding: Elf_Byte = STB_LOCAL, visibility: Elf_Byte = STV_DEFAULT): Sym {
            return when (ei_class) {
                E_IDENT.ELFCLASS32 -> ELF32_Sym(st_name, st_shndx = shndx, st_info = ELF_ST_INFO(binding, type), st_other = ELF_ST_OTHER(visibility))
                E_IDENT.ELFCLASS64 -> ELF64_Sym(st_name, st_shndx = shndx, st_info = ELF_ST_INFO(binding, type), st_other = ELF_ST_OTHER(visibility))
                else -> throw ELFGenerator.InvalidElfClassException(ei_class)
            }
        }

        fun size(ei_class: Elf_Byte): Elf_Half {
            return when (ei_class) {
                E_IDENT.ELFCLASS32 -> 16U
                E_IDENT.ELFCLASS64 -> 24U
                else -> throw ELFGenerator.InvalidElfClassException(ei_class)
            }
        }

        fun getSymbolType(st_info: Elf_Byte): String = when (ELF_ST_TYPE(st_info)) {
            STT_NOTYPE -> "NOTYPE"
            STT_OBJECT -> "OBJECT"
            STT_FUNC -> "FUNC"
            STT_SECTION -> "SECTION"
            STT_FILE -> "FILE"
            STT_COMMON -> "COMMON"
            STT_TLS -> "TLS"
            STT_NUM -> "NUM"
            STT_LOOS -> "LOOS"
            STT_GNU_IFUNC -> "GNU IFUNC"
            STT_HIOS -> "HIOS"
            STT_LOPROC -> "LOPROC"
            STT_HIPROC -> "HIPROC"
            else -> "UNKNOWN"
        }

        fun getSymbolBinding(st_info: Elf_Byte): String = when (ELF_ST_BIND(st_info)) {
            STB_LOCAL -> "LOCAL"
            STB_GLOBAL -> "GLOBAL"
            STB_WEAK -> "WEAK"
            STB_NUM -> "NUM"
            STB_LOOS -> "LOOS"
            STB_GNU_UNIQUE -> "GNU UNIQUE"
            STB_HIOS -> "HIOS"
            STB_LOPROC -> "LOPROC"
            STB_HIPROC -> "HIPROC"
            else -> "UNKNOWN"
        }

        fun getSymbolVisibility(st_other: Elf_Byte): String = when (ELF_ST_VISIBILITY(st_other)) {
            STV_DEFAULT -> "DEFAULT"
            STV_INTERNAL -> "INTERNAL"
            STV_HIDDEN -> "HIDDEN"
            STV_PROTECTED -> "PROTECTED"
            else -> "UNKNOWN"
        }

        fun getSymbolSectionIndex(st_shndx: Elf_Half): String = when (st_shndx) {
            Shdr.SHN_UNDEF -> "UND"
            Shdr.SHN_ABS -> "ABS"
            Shdr.SHN_COMMON -> "COM"
            else -> st_shndx.toString()
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Sym {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val st_name = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val st_value = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val st_size = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val st_info = byteArray.loadUByte(currIndex)
                    currIndex += 1
                    val st_other = byteArray.loadUByte(currIndex)
                    currIndex += 1
                    val st_shndx = byteArray.loadUShort(eIdent, currIndex)

                    return ELF32_Sym(st_name, st_value, st_size, st_info, st_other, st_shndx)
                }

                E_IDENT.ELFCLASS64 -> {
                    val st_name = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val st_info = byteArray.loadUByte(currIndex)
                    currIndex += 1
                    val st_other = byteArray.loadUByte(currIndex)
                    currIndex += 1
                    val st_shndx = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2
                    val st_value = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val st_size = byteArray.loadULong(eIdent, currIndex)

                    return ELF64_Sym(st_name, st_info, st_other, st_shndx, st_value, st_size)
                }

                else -> throw NotInELFFormatException
            }
        }


        const val STN_UNDEF = 0U

        /**
         * Symbol Binding
         */

        fun ELF_ST_BIND(st_info: Elf_Byte): Elf_Byte = st_info.toUInt().shr(4).toUByte()
        fun ELF_ST_TYPE(st_info: Elf_Byte): Elf_Byte = st_info.and(0xfU)
        fun ELF_ST_VISIBILITY(st_other: Elf_Byte): Elf_Byte = st_other and 0b11U
        fun ELF_ST_INFO(b: Elf_Byte, t: Elf_Byte): Elf_Byte = (b.toUInt().shl(4) + t.and(0xfU)).toUByte()
        fun ELF_ST_OTHER(st_visibility: Elf_Byte): Elf_Byte = (st_visibility and 0b11U)

        /**
         * Local symbols are not visible outside the object file containing their
         * definition. Local symbols of the same name may exist in multiple files
         * without interfering with each other.
         *
         * In each symbol table, all symbols with [STB_LOCAL] binding precede the weak and global
         * symbols. A symbol's type provides a general classification for the associated entity.
         */
        const val STB_LOCAL: Elf_Byte = 0U

        /**
         * Global symbols are visible to all object files being combined. One file's
         * definition of a global symbol will satisfy another file's undefined reference
         * to the same global symbol.
         */
        const val STB_GLOBAL: Elf_Byte = 1U

        /**
         * Weak symbols resemble global symbols, but their definitions have lower
         * precedence.
         */
        const val STB_WEAK: Elf_Byte = 2U

        /**
         * Number of defined types.
         */
        const val STB_NUM: Elf_Byte = 3U

        /**
         * Start of OS-Specific
         */
        const val STB_LOOS: Elf_Byte = 0x10U

        /**
         * Unique Symbol
         */
        const val STB_GNU_UNIQUE: Elf_Byte = 0x10U

        /**
         * End of OS specific
         */
        const val STB_HIOS: Elf_Byte = 0x12U

        /**
         * [STB_LOPROC] .. [STB_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val STB_LOPROC: Elf_Byte = 13U

        /**
         * [STB_LOPROC] .. [STB_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val STB_HIPROC: Elf_Byte = 15U

        /**
         * Symbol Types
         */

        /**
         * The symbol's type is not specified.
         */
        const val STT_NOTYPE: Elf_Byte = 0U

        /**
         * The symbol is associated with a data object, such as a variable, an array,
         * and so on.
         */
        const val STT_OBJECT: Elf_Byte = 1U

        /**
         * The symbol is associated with a function or other executable code.
         */
        const val STT_FUNC: Elf_Byte = 2U

        /**
         * The symbol is associated with a section. Symbol table entries of this type
         * exist primarily for relocation and normally have STB_LOCAL binding.
         */
        const val STT_SECTION: Elf_Byte = 3U

        /**
         * A file symbol has [STB_LOCAL] binding, its section index is [SHN_ABS], and
         * it precedes the other [STB_LOCAL] symbols for the file, if it is present.
         */
        const val STT_FILE: Elf_Byte = 4U

        /**
         * Common data object
         */
        const val STT_COMMON: Elf_Byte = 5U

        /**
         * Threat local data object
         */
        const val STT_TLS: Elf_Byte = 6U

        /**
         * Number of defined types
         */
        const val STT_NUM: Elf_Byte = 7U

        /**
         * Start of OS-Specific
         */
        const val STT_LOOS: Elf_Byte = 0x10U

        /**
         * Indirect Code Object
         */
        const val STT_GNU_IFUNC: Elf_Byte = 0x10U

        /**
         * End of OS-Specific
         */
        const val STT_HIOS: Elf_Byte = 0x12U

        /**
         * [STT_LOPROC] .. [STT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         *  If a symbol's value refers to a specific location within a section, its section
         * index member, st_shndx, holds an index into the section header table.
         * As the section moves during relocation, the symbol's value changes as well,
         * and references to the symbol continue to "point'' to the same location in the
         * program. Some special section index values give other semantics.
         */
        const val STT_LOPROC: Elf_Byte = 13U

        /**
         * [STT_LOPROC] .. [STT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         *  If a symbol's value refers to a specific location within a section, its section
         * index member, st_shndx, holds an index into the section header table.
         * As the section moves during relocation, the symbol's value changes as well,
         * and references to the symbol continue to "point'' to the same location in the
         * program. Some special section index values give other semantics.
         */
        const val STT_HIPROC: Elf_Byte = 15U

        /**
         * [st_other] Visibility
         */

        /**
         * Default visibility
         */
        const val STV_DEFAULT: Elf_Byte = 0U

        /**
         * Processor Specific Hidden class
         */
        const val STV_INTERNAL: Elf_Byte = 1U

        /**
         * Unavailable to other modules
         */
        const val STV_HIDDEN: Elf_Byte = 2U

        /**
         * Not exported
         */
        const val STV_PROTECTED: Elf_Byte = 3U


    }
}