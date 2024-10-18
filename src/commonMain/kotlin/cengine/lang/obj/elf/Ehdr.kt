package cengine.lang.obj.elf

/**
 * ELF Header
 *
 * @property e_ident The initial bytes mark the file as an object file and provide machine-independent data with which to decode and interpret the file's contents.
 * @property e_type This member identifies the object file type.
 * @property e_machine This member's value specifies the required architecture for an individual file.
 * @property e_version This member identifies the object file version.
 * @property e_entry This member gives the virtual address to which the system first transfers control, thus starting the process. If the file has no associated entry point, this member holds zero.
 * @property e_phoff This member holds the program header table's file offset in bytes. If the file has no
 * program header table, this member holds zero.
 * @property e_shoff This member holds the section header table's file offset in bytes. If the file has no
 * section header table, this member holds zero.
 * @property e_flags This member holds processor-specific flags associated with the file. Flag names
 * take the form [EF_machine_flag].
 * @property e_ehsize This member holds the ELF header's size in bytes.
 * @property e_phentsize This member holds the size in bytes of one entry in the file's program header table;
 * all entries are the same size.
 * @property e_phnum This member holds the number of entries in the program header table. Thus, the
 * product of [e_phentsize] and [e_phnum] gives the table's size in bytes. If a file
 * has no program header table, [e_phnum] holds value zero.
 * @property e_shentsize This member holds a section header's size in bytes. A section header is one entry
 * in the section header table; all entries are the same size.
 * @property e_shnum This member holds the number of entries in the section header table. Thus, the
 * product of [e_shentsize] and [e_shnum] gives the section header table's size in
 * bytes. If a file has no section header table, [e_shnum] holds value zero.
 * @property e_shstrndx This member holds the section header table index of the entry associated with the
 * section name string table. If the file has no section name string table, this member
 * holds the value [SHN_UNDEF]. See "Sections" and "String Table" below for more
 * information.
 *
 */
sealed interface Ehdr : BinaryProvider {

    var e_ident: E_IDENT
    var e_type: cengine.lang.obj.elf.Elf_Half
    var e_machine: cengine.lang.obj.elf.Elf_Half
    var e_version: cengine.lang.obj.elf.Elf_Word
    var e_flags: cengine.lang.obj.elf.Elf_Word
    var e_ehsize: cengine.lang.obj.elf.Elf_Half
    var e_phentsize: cengine.lang.obj.elf.Elf_Half
    var e_phnum: cengine.lang.obj.elf.Elf_Half
    var e_shentsize: cengine.lang.obj.elf.Elf_Half
    var e_shnum: cengine.lang.obj.elf.Elf_Half
    var e_shstrndx: cengine.lang.obj.elf.Elf_Half

    companion object {
        fun getELFType(type: cengine.lang.obj.elf.Elf_Half): String = when (type) {
            ET_NONE -> "NONE (No file type)"
            ET_REL -> "REL (Relocatable file)"
            ET_EXEC -> "EXEC (Executable file)"
            ET_DYN -> "DYN (Shared object file)"
            ET_CORE -> "CORE (Core file)"
            else -> "UNKNOWN (0x${type.toString(16)})"
        }

        fun getELFMachine(machine: cengine.lang.obj.elf.Elf_Half): String = when (machine) {
            EM_386 -> "Intel 80386"
            EM_VPP500 -> "VPP500"
            EM_SPARC -> "SPARC"
            EM_68K -> "68K"
            EM_88K -> "88K"
            EM_X86_64 -> "AMD x86-64 architecture"
            EM_ARM -> "ARM"
            EM_RISCV -> "RISCV"
            EM_CUSTOM_IKRMINI -> "IKR MINI"
            EM_CUSTOM_IKRRISC2 -> "IKR RISC-II"
            EM_CUSTOM_T6502 -> "T6502"
            else -> "UNKNOWN (0x${machine.toString(16)})"
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT): Ehdr {
            var currIndex = E_IDENT.EI_NIDENT.toInt()
            val e_type = byteArray.loadUShort(eIdent, currIndex)
            currIndex += 2
            val e_machine = byteArray.loadUShort(eIdent, currIndex)
            currIndex += 2
            val e_version = byteArray.loadUInt(eIdent, currIndex)
            currIndex += 4

            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val e_entry = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val e_phoff = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val e_shoff = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4

                    val e_flags = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4

                    val e_ehsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_phentsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_phnum = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shentsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shnum = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shstrndx = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    return ELF32_Ehdr(eIdent, e_type, e_machine, e_version, e_entry, e_phoff, e_shoff, e_flags, e_ehsize, e_phentsize, e_phnum, e_shentsize, e_shnum, e_shstrndx)
                }

                E_IDENT.ELFCLASS64 -> {
                    val e_entry = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val e_phoff = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val e_shoff = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8

                    val e_flags = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4

                    val e_ehsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_phentsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_phnum = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shentsize = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shnum = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    val e_shstrndx = byteArray.loadUShort(eIdent, currIndex)
                    currIndex += 2

                    return ELF64_Ehdr(eIdent, e_type, e_machine, e_version, e_entry, e_phoff, e_shoff, e_flags, e_ehsize, e_phentsize, e_phnum, e_shentsize, e_shnum, e_shstrndx)
                }

                else -> throw cengine.lang.obj.elf.NotInELFFormatException
            }

        }

        /**
         * [e_type]
         */

        /**
         * No file type
         */
        const val ET_NONE: cengine.lang.obj.elf.Elf_Half = 0U

        /**
         * Relocatable file
         */
        const val ET_REL: cengine.lang.obj.elf.Elf_Half = 1U

        /**
         * Executable file
         */
        const val ET_EXEC: cengine.lang.obj.elf.Elf_Half = 2U

        /**
         * Shared Object file
         */
        const val ET_DYN: cengine.lang.obj.elf.Elf_Half = 3U

        /**
         * Core file
         */
        const val ET_CORE: cengine.lang.obj.elf.Elf_Half = 4U

        /**
         * Processor-specific
         */
        const val ET_LOPROC: cengine.lang.obj.elf.Elf_Half = 0xFF00U

        /**
         * Processor-specific
         */
        const val ET_HIPROC: cengine.lang.obj.elf.Elf_Half = 0xFFFFU

        /**
         * [e_machine]
         */

        /**
         * AT&T WE 32100
         */
        const val EM_M32: cengine.lang.obj.elf.Elf_Half = 1U

        /**
         * SPARC
         */
        const val EM_SPARC: cengine.lang.obj.elf.Elf_Half = 2U // SPARC

        /**
         * Intel Architecture
         */
        const val EM_386: cengine.lang.obj.elf.Elf_Half = 3U // Intel Architecture

        /**
         * Motorola 68000
         */
        const val EM_68K: cengine.lang.obj.elf.Elf_Half = 4U // Motorola 68000

        /**
         * Motorola 88000
         */
        const val EM_88K: cengine.lang.obj.elf.Elf_Half = 5U // Motorola 88000

        /**
         * Intel 80860
         */
        const val EM_860: cengine.lang.obj.elf.Elf_Half = 7U

        /**
         * MIPS RS3000 Big-Endian
         */
        const val EM_MIPS: cengine.lang.obj.elf.Elf_Half = 8U

        /**
         * MIPS RS4000 Big-Endian
         */
        const val EM_MIPS_RS4_BE: cengine.lang.obj.elf.Elf_Half = 10U

        /**
         * Fujitsu VPP500
         */
        const val EM_VPP500: cengine.lang.obj.elf.Elf_Half = 17U

        /**
         * ARM
         */
        const val EM_ARM: cengine.lang.obj.elf.Elf_Half = 40U

        /**
         * X86 64Bit
         */
        const val EM_X86_64: cengine.lang.obj.elf.Elf_Half = 62U

        /**
         * RISC-V
         */
        const val EM_RISCV: cengine.lang.obj.elf.Elf_Half = 243U

        // ...

        // FREE TO USE MACHINE TYPES 0xFF00 - 0xFFFF

        const val EM_CUSTOM_IKRRISC2: cengine.lang.obj.elf.Elf_Half = 0xFF00U

        const val EM_CUSTOM_IKRMINI: cengine.lang.obj.elf.Elf_Half = 0xFF01U

        const val EM_CUSTOM_T6502: cengine.lang.obj.elf.Elf_Half = 0xFF02U


        /**
         * Invalid version
         */
        const val EV_NONE: cengine.lang.obj.elf.Elf_Word = 0U

        /**
         * Current version
         */
        const val EV_CURRENT: cengine.lang.obj.elf.Elf_Word = 1U
    }

    override fun toString(): String

}