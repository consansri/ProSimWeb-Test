package cengine.lang.asm.elf

/**
 * ELF Program Header
 *
 * An executable or shared object file's program header table is an array of structures, each
 * describing a segment or other information the system needs to prepare the program for
 * execution. An object file segment contains one or more sections. Program headers are
 * meaningful only for executable and shared object files. A file specifies its own program header
 * size with the ELF header's [e_phentsize] and  [e_phnum] members.
 *
 * @property p_type This member tells what kind of segment this array element describes or how to
 * interpret the array element's information. Type values and their meanings appear
 * below.
 *
 * @property p_offset This member gives the offset from the beginning of the file at which the first byte
 * of the segment resides.
 *
 * @property p_vaddr This member gives the virtual address at which the first byte of the segment resides
 * in memory.
 *
 * @property p_paddr On systems for which physical addressing is relevant, this member is reserved for
 * the segment's physical address. This member requires operating system specific
 * information, which is described in the appendix at the end of Book III.
 *
 * @property p_filesz This member gives the number of bytes in the file image of the segment; it may be
 * zero.
 *
 * @property p_memsz This member gives the number of bytes in the memory image of the segment; it
 * may be zero.
 *
 * @property p_flags This member gives flags relevant to the segment. Defined flag values appear below.
 *
 * @property p_align Loadable process segments must have congruent values for [p_vaddr] and
 * [p_offset], modulo the page size. This member gives the value to which the
 * segments are aligned in memory and in the file. Values 0 and 1 mean that no
 * alignment is required. Otherwise, [p_align] should be a positive, integral power of
 * 2, and [p_addr] should equal [p_offset], modulo [p_align].
 *
 *
 */
sealed interface Phdr : BinaryProvider {

    var p_type: Elf_Word
    var p_flags: Elf_Word

    companion object {
        fun size(ei_class: Elf_Byte): Elf_Half{
            return when(ei_class){
                E_IDENT.ELFCLASS32 -> 32U
                E_IDENT.ELFCLASS64 -> 56U
                else -> throw ELFBuilder.InvalidElfClassException(ei_class)
            }
        }

        fun getProgramHeaderType(type: Elf_Word): String = when (type) {
            PT_NULL -> "NULL"
            PT_LOAD -> "LOAD"
            PT_DYNAMIC -> "DYNAMIC"
            PT_INTERP -> "INTERP"
            PT_NOTE -> "NOTE"
            PT_SHLIB -> "SHLIB"
            PT_PHDR -> "PHDR"
            PT_TLS -> "TLS"
            in PT_LOOS..PT_HIOS -> "OS"
            in PT_LOPROC..PT_HIPROC -> "PROC"
            else -> "UNKNOWN (0x${type.toString(16)})"
        }

        fun getProgramHeaderFlags(flags: Elf_Word): String {
            val flagsList = mutableListOf<String>()
            if (flags and PF_X != 0U) flagsList.add("X")
            if (flags and PF_W != 0U) flagsList.add("W")
            if (flags and PF_R != 0U) flagsList.add("R")
            if (flags and PF_MASKPROC != 0U) flagsList.add("PROC")
            if (flags and PF_MASKOS != 0U) flagsList.add("OS")
            return flagsList.joinToString(" ")
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Phdr {
            var currIndex = offset
            val p_type = byteArray.loadUInt(eIdent, currIndex)
            currIndex += 4

            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val p_offset = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_vaddr = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_paddr = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_filesz = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_memsz = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_flags = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_align = byteArray.loadUInt(eIdent, currIndex)

                    return ELF32_Phdr(p_type, p_offset, p_vaddr, p_paddr, p_filesz, p_memsz, p_flags, p_align)
                }

                E_IDENT.ELFCLASS64 -> {
                    val p_flags = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val p_offset = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val p_vaddr = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val p_paddr = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val p_filesz = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val p_memsz = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val p_align = byteArray.loadULong(eIdent, currIndex)

                    return ELF64_Phdr(p_type, p_flags, p_offset, p_vaddr, p_paddr, p_filesz, p_memsz, p_align)
                }

                else -> throw NotInELFFormatException
            }
        }

        /**
         * [p_type]
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
         * The array element specifies the Thread-Local Storage template. Implementations need not support this program table entry.
         */
        const val PT_TLS: Elf_Word = 7U

        /**
         * [PT_LOOS] .. [PT_HIOS] : Values in this inclusive range are reserved for operating system-specific semantics.
         */
        const val PT_LOOS: Elf_Word = 0x60000000U

        /**
         * [PT_LOOS] .. [PT_HIOS] : Values in this inclusive range are reserved for operating system-specific semantics.
         */
        const val PT_HIOS: Elf_Word = 0x6fffffffU

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_LOPROC: Elf_Word = 0x70000000U

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_HIPROC: Elf_Word = 0x7fffffffU

        /**
         * [p_flags]
         */

        /**
         * Execute
         */
        const val PF_X: Elf_Word = 0x1U

        /**
         * Write
         */
        const val PF_W: Elf_Word = 0x2U

        /**
         * Read
         */
        const val PF_R: Elf_Word = 0x4U

        /**
         * Unspecified
         */
        const val PF_MASKOS: Elf_Word = 0x0ff00000U

        /**
         * Unspecified
         */
        const val PF_MASKPROC: Elf_Word = 0xf0000000U

    }

}