package cengine.lang.asm.elf32

/**
 * Symbol Table
 *
 * An object file's symbol table holds information needed to locate and relocate a program's
 * symbolic definitions and references.  A symbol table index is a subscript into this array. Index
 * 0 both designates the first entry in the table and serves as the undefined symbol index. The
 * contents of the initial entry are specified later in this section.
 *
 * @param st_name This member holds an index into the object file's symbol string table, which holds
 * the character representations of the symbol names.
 *
 * @param st_value This member gives the value of the associated symbol. Depending on the context,
 * this may be an absolute value, an address, and so on; details appear below.
 *
 * @param st_size Many symbols have associated sizes. For example, a data object's size is the number
 * of bytes contained in the object. This member holds 0 if the symbol has no size or
 * an unknown size.
 *
 * @param st_info This member specifies the symbol's type and binding attributes. A list of the values
 * and meanings appears below. The following code shows how to manipulate the
 * values.
 *
 * @param st_other This member currently holds 0 and has no defined meaning.
 *
 * @param st_shndx Every symbol table entry is "defined'' in relation to some section; this member holds
 * the relevant section header table index. As Figure 1-7 and the related text describe,
 * some section indexes indicate special meanings.
 *
 */
data class ELF32_Sym(
    var st_name: ELF32_WORD,
    var st_value: ELF32_ADDR,
    var st_size: ELF32_WORD,
    var st_info: ELF32_UnsignedChar,
    var st_other: ELF32_UnsignedChar,
    var st_shndx: ELF32_HALF
): BinaryProvider {
    companion object {
        const val STN_UNDEF = 0U

        /**
         * Symbol Binding
         */

        fun ELF32_ST_BIND(i: ELF32_WORD) = i.shr(4)
        fun ELF32_ST_TYPE(i: ELF32_WORD) = i.and(0xfU)
        fun ELF32_ST_INFO(b: ELF32_WORD, t: ELF32_WORD) = b.shl(4) + t.and(0xfU)

        /**
         * Local symbols are not visible outside the object file containing their
         * definition. Local symbols of the same name may exist in multiple files
         * without interfering with each other.
         *
         * In each symbol table, all symbols with [STB_LOCAL] binding precede the weak and global
         * symbols. A symbol's type provides a general classification for the associated entity.
         */
        const val STB_LOCAL: ELF32_WORD = 0U

        /**
         * Global symbols are visible to all object files being combined. One file's
         * definition of a global symbol will satisfy another file's undefined reference
         * to the same global symbol.
         */
        const val STB_GLOBAL: ELF32_WORD = 1U

        /**
         * Weak symbols resemble global symbols, but their definitions have lower
         * precedence.
         */
        const val STB_WEAK: ELF32_WORD = 2U

        /**
         * [STB_LOPROC] .. [STB_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val STB_LOPROC: ELF32_WORD = 13U

        /**
         * [STB_LOPROC] .. [STB_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val STB_HIPROC: ELF32_WORD = 15U

        /**
         * Symbol Types
         */

        /**
         * The symbol's type is not specified.
         */
        const val STT_NOTYPE: ELF32_WORD = 0U

        /**
         * The symbol is associated with a data object, such as a variable, an array,
         * and so on.
         */
        const val STT_OBJECT: ELF32_WORD = 1U

        /**
         * The symbol is associated with a function or other executable code.
         */
        const val STT_FUNC: ELF32_WORD = 2U

        /**
         * The symbol is associated with a section. Symbol table entries of this type
         * exist primarily for relocation and normally have STB_LOCAL binding.
         */
        const val STT_SECTION: ELF32_WORD = 3U

        /**
         * A file symbol has [STB_LOCAL] binding, its section index is [SHN_ABS], and
         * it precedes the other [STB_LOCAL] symbols for the file, if it is present.
         */
        const val STT_FILE: ELF32_WORD = 4U

        /**
         * [STT_LOPROC] .. [STT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         *  If a symbol's value refers to a specific location within a section, its section
         * index member, st_shndx, holds an index into the section header table.
         * As the section moves during relocation, the symbol's value changes as well,
         * and references to the symbol continue to "point'' to the same location in the
         * program. Some special section index values give other semantics.
         */
        const val STT_LOPROC: ELF32_WORD = 13U

        /**
         * [STT_LOPROC] .. [STT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         *  If a symbol's value refers to a specific location within a section, its section
         * index member, st_shndx, holds an index into the section header table.
         * As the section moves during relocation, the symbol's value changes as well,
         * and references to the symbol continue to "point'' to the same location in the
         * program. Some special section index values give other semantics.
         */
        const val STT_HIPROC: ELF32_WORD = 15U

    }

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }

}