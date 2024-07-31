package cengine.lang.asm.elf

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
interface Dyn: BinaryProvider {

    companion object{
        /**
         * Marks end of dynamic section.
         */
        const val DT_NULL = 0

        /**
         * String table offset to name of a needed library.
         */
        const val DT_NEEDED = 1

        /**
         * Size in bytes of PLT relocation entries.
         */
        const val DT_PLTRELSZ = 2

        /**
         * Address of PLT and/or GOT.
         */
        const val DT_PLTGOT = 3

        /**
         * Address of symbol hash table.
         */
        const val DT_HASH = 4

        /**
         * Address of string table.
         */
        const val DT_STRTAB = 5

        /**
         * Address of symbol table.
         */
        const val DT_SYMTAB = 6

        /**
         * Address of Rela relocation table.
         */
        const val DT_RELA = 7

        /**
         * Size in bytes of the Rela relocation table.
         */
        const val DT_RELASZ = 8

        /**
         * Size in bytes of a Rela relocation table entry.
         */
        const val DT_RELAENT = 9

        /**
         * Size in bytes of string table.
         */
        const val DT_STRSZ = 10

        /**
         * Size in bytes of a symbol table entry.
         */
        const val DT_SYMENT = 11

        /**
         * Address of the initialization function.
         */
        const val DT_INIT = 12

        /**
         * Address of the termination function.
         */
        const val DT_FINI = 13

        /**
         * String table offset to name of shared object.
         */
        const val DT_SONAME = 14

        /**
         * String table offset to search path for direct and indirect library dependencies.
         */
        const val DT_RPATH = 15

        /**
         * Alert linker to search this shared object before the executable for symbols.
         */
        const val DT_SYMBOLIC = 16

        /**
         * Address of Rel relocation table.
         */
        const val DT_REL = 17

        /**
         * Size in bytes of Rel relocation table.
         */
        const val DT_RELSZ = 18

        /**
         * Size in bytes of a Rel table entry.
         */
        const val DT_RELENT = 19

        /**
         * Type of relocation entry to which the PLT refers (Rela or Rel).
         */
        const val DT_PLTREL = 20

        /**
         * Undefined use for debugging.
         */
        const val DT_DEBUG = 21

        /**
         * Absence of this entry indicates that no relocation entries should apply to a nonwritable segment.
         */
        const val DT_TEXTREL = 22

        /**
         * Address of relocation entries associated solely with the PLT.
         */
        const val DT_JMPREL = 23

        /**
         * Instruct dynamic linker to process all relocations before transferring control to the executable.
         */
        const val DT_BIND_NOW = 24

        /**
         * String table offset to search path for direct library dependencies.
         */
        const val DT_RUNPATH = 25

        /**
         * Lower bound of processor-specific semantic values.
         */
        const val DT_LOPROC = 0x70000000

        /**
         * Upper bound of processor-specific semantic values.
         */
        const val DT_HIPROC = 0x7fffffff
    }

}