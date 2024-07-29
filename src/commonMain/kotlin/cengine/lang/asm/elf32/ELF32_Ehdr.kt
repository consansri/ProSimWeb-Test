package cengine.lang.asm.elf32


/**
 * ELF Header
 *
 * @param e_ident The initial bytes mark the file as an object file and provide machine-independent data with which to decode and interpret the file's contents.
 * @param e_type This member identifies the object file type.
 * @param e_machine This member's value specifies the required architecture for an individual file.
 * @param e_version This member identifies the object file version.
 * @param e_entry This member gives the virtual address to which the system first transfers control, thus starting the process. If the file has no associated entry point, this member holds zero.
 * @param e_phoff This member holds the program header table's file offset in bytes. If the file has no
 * program header table, this member holds zero.
 * @param e_shoff This member holds the section header table's file offset in bytes. If the file has no
 * section header table, this member holds zero.
 * @param e_flags This member holds processor-specific flags associated with the file. Flag names
 * take the form [EF_machine_flag].
 * @param e_ehsize This member holds the ELF header's size in bytes.
 * @param e_phentsize This member holds the size in bytes of one entry in the file's program header table;
 * all entries are the same size.
 * @param e_phnum This member holds the number of entries in the program header table. Thus, the
 * product of [e_phentsize] and [e_phnum] gives the table's size in bytes. If a file
 * has no program header table, [e_phnum] holds value zero.
 * @param e_shentsize This member holds a section header's size in bytes. A section header is one entry
 * in the section header table; all entries are the same size.
 * @param e_shnum This member holds the number of entries in the section header table. Thus, the
 * product of [e_shentsize] and [e_shnum] gives the section header table's size in
 * bytes. If a file has no section header table, [e_shnum] holds value zero.
 * @param e_shstrndx This member holds the section header table index of the entry associated with the
 * section name string table. If the file has no section name string table, this member
 * holds the value [SHN_UNDEF]. See "Sections" and "String Table" below for more
 * information.
 *
 */
data class ELF32_Ehdr(
    var e_ident: E_IDENT,
    var e_type: ELF32_HALF,
    var e_machine: ELF32_HALF,
    var e_version: ELF32_WORD = EV_CURRENT,
    var e_entry: ELF32_ADDR,
    var e_phoff: ELF32_OFF,
    var e_shoff: ELF32_OFF,
    var e_flags: ELF32_WORD,
    var e_ehsize: ELF32_HALF,
    var e_phentsize: ELF32_HALF,
    var e_phnum: ELF32_HALF,
    var e_shentsize: ELF32_HALF,
    var e_shnum: ELF32_HALF,
    var e_shstrndx: ELF32_HALF
): BinaryProvider {

    /*constructor(
        ei_class: ELF32_UnsignedChar,
        ei_data: ELF32_UnsignedChar,
        e_type: ELF32_HALF,

    ): this(
        E_IDENT(ei_class = ei_class, ei_data = ei_data),
        e_type
    )*/

    companion object {

        /**
         * [e_type]
         */

        /**
         * No file type
         */
        const val ET_NONE: ELF32_HALF = 0U

        /**
         * Relocatable file
         */
        const val ET_REL: ELF32_HALF = 1U

        /**
         * Executable file
         */
        const val ET_EXEC: ELF32_HALF = 2U

        /**
         * Shared Object file
         */
        const val ET_DYN: ELF32_HALF = 3U

        /**
         * Core file
         */
        const val ET_CORE: ELF32_HALF = 4U

        /**
         * Processor-specific
         */
        const val ET_LOPROC: ELF32_HALF = 0xFF00U

        /**
         * Processor-specific
         */
        const val ET_HIPROC: ELF32_HALF = 0xFFFFU

        /**
         * [e_machine]
         */

        /**
         * AT&T WE 32100
         */
        const val EM_M32: ELF32_HALF = 1U

        /**
         * SPARC
         */
        const val EM_SPARC: ELF32_HALF = 2U // SPARC

        /**
         * Intel Architecture
         */
        const val EM_386: ELF32_HALF = 3U // Intel Architecture

        /**
         * Motorola 68000
         */
        const val EM_68K: ELF32_HALF = 4U // Motorola 68000

        /**
         * Motorola 88000
         */
        const val EM_88K: ELF32_HALF = 5U // Motorola 88000

        /**
         * Intel 80860
         */
        const val EM_860: ELF32_HALF = 7U

        /**
         * MIPS RS3000 Big-Endian
         */
        const val EM_MIPS: ELF32_HALF = 8U

        /**
         * MIPS RS4000 Big-Endian
         */
        const val EM_MIPS_RS4_BE: ELF32_HALF = 10U

        /**
         * [e_machine]
         */

        /**
         * Invalid version
         */
        const val EV_NONE: ELF32_WORD = 0U

        /**
         * Current version
         */
        const val EV_CURRENT: ELF32_WORD = 1U
    }

    /**
     * @param ei_mag0 File identification
     * @param ei_mag1 File identification
     * @param ei_mag2 File identification
     * @param ei_mag3 File identification
     * @param ei_class File class
     * @param ei_data Data encoding
     * @param ei_version File version
     * @param ei_pad Start of padding bytes
     * @param ei_nident Size of [E_IDENT.build]
     */
    data class E_IDENT(
        val ei_mag0: ELF32_UnsignedChar = ELFMAG0,
        val ei_mag1: ELF32_UnsignedChar = ELFMAG1,
        val ei_mag2: ELF32_UnsignedChar = ELFMAG2,
        val ei_mag3: ELF32_UnsignedChar = ELFMAG3,
        val ei_class: ELF32_UnsignedChar,
        val ei_data: ELF32_UnsignedChar,
        val ei_version: ELF32_UnsignedChar = EV_CURRENT.toByte(),
        val ei_pad: ELF32_UnsignedChar = ZERO,
        val ei_nident: ELF32_UnsignedChar = EI_NIDENT
    ) {
        companion object {
            const val ZERO: ELF32_UnsignedChar = 0

            const val EI_NIDENT: ELF32_UnsignedChar = 16

            const val ELFMAG0: ELF32_UnsignedChar = 0x7f

            /**
             * E in 7 bit ascii
             */
            const val ELFMAG1: ELF32_UnsignedChar = 0x45

            /**
             * L in 7 bit ascii
             */
            const val ELFMAG2: ELF32_UnsignedChar = 0x4C

            /**
             * F in 7 bit ascii
             */
            const val ELFMAG3: ELF32_UnsignedChar = 0x46

            /**
             * Invalid class
             */
            const val ELFCLASSNONE: ELF32_UnsignedChar = 0

            /**
             * 32-bit objects
             */
            const val ELFCLASS32: ELF32_UnsignedChar = 1

            /**
             * 64-bit objects
             */
            const val ELFCLASS64: ELF32_UnsignedChar = 2

            /**
             * Invalid data encoding
             */
            const val ELFDATANONE: ELF32_UnsignedChar = 0

            /**
             * Least significant byte occupying the lowest address.
             */
            const val ELFDATA2LSB: ELF32_UnsignedChar = 1

            /**
             * The most significant byte occupying the lowest address.
             */
            const val ELFDATA2MSB: ELF32_UnsignedChar = 2
        }

        fun build(): Array<ELF32_UnsignedChar> {
            val beginning = arrayOf(ei_mag0, ei_mag1, ei_mag2, ei_mag3, ei_class, ei_data, ei_version, ei_pad)
            val result = beginning + Array(ei_nident - beginning.size) {
                ZERO
            }
            return result
        }
    }

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }


}
