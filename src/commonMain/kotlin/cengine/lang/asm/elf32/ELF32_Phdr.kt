package cengine.lang.asm.elf32

/**
 * ELF Program Header
 *
 * An executable or shared object file's program header table is an array of structures, each
 * describing a segment or other information the system needs to prepare the program for
 * execution. An object file segment contains one or more sections. Program headers are
 * meaningful only for executable and shared object files. A file specifies its own program header
 * size with the ELF header's [e_phentsize] and  [e_phnum] members.
 *
 * @param p_type This member tells what kind of segment this array element describes or how to
 * interpret the array element's information. Type values and their meanings appear
 * below.
 *
 * @param p_offset This member gives the offset from the beginning of the file at which the first byte
 * of the segment resides.
 *
 * @param p_vaddr This member gives the virtual address at which the first byte of the segment resides
 * in memory.
 *
 * @param p_paddr On systems for which physical addressing is relevant, this member is reserved for
 * the segment's physical address. This member requires operating system specific
 * information, which is described in the appendix at the end of Book III.
 *
 * @param p_filesz This member gives the number of bytes in the file image of the segment; it may be
 * zero.
 *
 * @param p_memsz This member gives the number of bytes in the memory image of the segment; it
 * may be zero.
 *
 * @param p_flags This member gives flags relevant to the segment. Defined flag values appear below.
 *
 * @param p_align Loadable process segments must have congruent values for [p_vaddr] and
 * [p_offset], modulo the page size. This member gives the value to which the
 * segments are aligned in memory and in the file. Values 0 and 1 mean that no
 * alignment is required. Otherwise, [p_align] should be a positive, integral power of
 * 2, and [p_addr] should equal [p_offset], modulo [p_align].
 *
 *
 */
data class ELF32_Phdr(
    var p_type: ELF32_WORD,
    var p_offset: ELF32_OFF,
    var p_vaddr: ELF32_ADDR,
    var p_paddr: ELF32_ADDR,
    var p_filesz: ELF32_WORD,
    var p_memsz: ELF32_WORD,
    var p_flags: ELF32_WORD,
    var p_align: ELF32_WORD
): BinaryProvider {

    companion object{
        /**
         * Segment Types
         */

        /**
         * The array element is unused; other members' values are undefined. This type lets
         * the program header table have ignored entries.
         */
        const val PT_NULL: ELF32_WORD = 0U

        /**
         * The array element specifies a loadable segment, described by [p_filesz] and
         * [p_memsz]. The bytes from the file are mapped to the beginning of the memory
         * segment. If the segment's memory size ([p_memsz]) is larger than the file size
         * ([p_filesz]), the "extra'' bytes are defined to hold the value 0 and to follow the
         * segment's initialized area.  The file size may not be larger than the memory size.
         * Loadable segment entries in the program header table appear in ascending order,
         * sorted on the [p_vaddr] member.
         */
        const val PT_LOAD: ELF32_WORD = 1U

        /**
         * The array element specifies dynamic linking information.
         */
        const val PT_DYNAMIC: ELF32_WORD = 2U

        /**
         * The array element specifies the location and size of a null-terminated path name to
         * invoke as an interpreter.
         */
        const val PT_INTERP: ELF32_WORD = 3U

        /**
         * The array element specifies the location and size of auxiliary information.
         */
        const val PT_NOTE: ELF32_WORD = 4U

        /**
         * This segment type is reserved but has unspecified semantics.
         */
        const val PT_SHLIB: ELF32_WORD = 5U

        /**
         * The array element, if present, specifies the location and size of the program header
         * table itself, both in the file and in the memory image of the program.  This segment
         * type may not occur more than once in a file. Moreover, it may occur only if the
         * program header table is part of the memory image of the program.  If it is present,
         * it must precede any loadable segment entry. See "Program Interpreter" in the
         * appendix at the end of Book III for further information.
         */
        const val PT_PHDR: ELF32_WORD = 6U

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_LOPROC: ELF32_WORD = 0x70000000U

        /**
         * [PT_LOPROC] .. [PT_HIPROC] : Values in this inclusive range are reserved for processor-specific semantics.
         */
        const val PT_HIPROC: ELF32_WORD = 0x7fffffffU
    }

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }
}