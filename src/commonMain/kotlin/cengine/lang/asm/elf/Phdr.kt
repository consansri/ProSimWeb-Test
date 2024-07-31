package cengine.lang.asm.elf

import cengine.lang.asm.elf.elf32.BinaryProvider

interface Phdr: BinaryProvider {


    companion object{
        /**
         * Segment Types
         */

        /**
         * The array element is unused; other members' values are undefined. This type lets
         * the program header table have ignored entries.
         */
        const val PT_NULL: Elf_Word = 0U

        /**
         * The array element specifies a loadable segment, described by [p_filesz] and
         * [p_memsz]. The bytes from the file are mapped to the beginning of the memory
         * segment. If the segment's memory size ([p_memsz]) is larger than the file size
         * ([p_filesz]), the "extra'' bytes are defined to hold the value 0 and to follow the
         * segment's initialized area.  The file size may not be larger than the memory size.
         * Loadable segment entries in the program header table appear in ascending order,
         * sorted on the [p_vaddr] member.
         */
        const val PT_LOAD: Elf_Word = 1U

        /**
         * The array element specifies dynamic linking information.
         */
        const val PT_DYNAMIC: Elf_Word = 2U

        /**
         * The array element specifies the location and size of a null-terminated path name to
         * invoke as an interpreter.
         */
        const val PT_INTERP: Elf_Word = 3U

        /**
         * The array element specifies the location and size of auxiliary information.
         */
        const val PT_NOTE: Elf_Word = 4U

        /**
         * This segment type is reserved but has unspecified semantics.
         */
        const val PT_SHLIB: Elf_Word = 5U

        /**
         * The array element, if present, specifies the location and size of the program header
         * table itself, both in the file and in the memory image of the program.  This segment
         * type may not occur more than once in a file. Moreover, it may occur only if the
         * program header table is part of the memory image of the program.  If it is present,
         * it must precede any loadable segment entry. See "Program Interpreter" in the
         * appendix at the end of Book III for further information.
         */
        const val PT_PHDR: Elf_Word = 6U

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_LOPROC: Elf_Word = 0x70000000U

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_HIPROC: Elf_Word = 0x7fffffffU
    }

}