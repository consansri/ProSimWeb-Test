package cengine.lang.asm.elf

import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr

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
interface Ehdr : BinaryProvider {

    var e_ident: E_IDENT
    var e_type: Elf_Half
    var e_machine: Elf_Half
    var e_version: Elf_Word
    var e_flags: Elf_Word
    var e_ehsize: Elf_Half
    var e_phentsize: Elf_Half
    var e_phnum: Elf_Half
    var e_shentsize: Elf_Half
    var e_shnum: Elf_Half
    var e_shstrndx: Elf_Half

    companion object {

        private fun getELFType(type: Elf_Half): String = when (type) {
            ET_NONE -> "NONE (No file type)"
            ET_REL -> "REL (Relocatable file)"
            ET_EXEC -> "EXEC (Executable file)"
            ET_DYN -> "DYN (Shared object file)"
            ET_CORE -> "CORE (Core file)"
            else -> "UNKNOWN (0x${type.toString(16)})"
        }

        private fun getELFMachine(machine: Elf_Half): String = when (machine) {
            EM_386 -> "Intel 80386"
            EM_VPP500 -> "VPP500"
            EM_SPARC -> "SPARC"
            EM_68K -> "68K"
            EM_88K -> "88K"
            EM_X86_64 -> "AMD x86-64 architecture"
            EM_ARM -> "ARM"
            EM_RISCV -> "RISCV"
            else -> "UNKNOWN (0x${machine.toString(16)})"
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT): Ehdr {
            val eIdentSize = eIdent.ei_nident.toInt()
            var currIndex = eIdentSize
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

                else -> throw NotInELFFormatException
            }

        }

        /**
         * [e_type]
         */

        /**
         * No file type
         */
        const val ET_NONE: Elf_Half = 0U

        /**
         * Relocatable file
         */
        const val ET_REL: Elf_Half = 1U

        /**
         * Executable file
         */
        const val ET_EXEC: Elf_Half = 2U

        /**
         * Shared Object file
         */
        const val ET_DYN: Elf_Half = 3U

        /**
         * Core file
         */
        const val ET_CORE: Elf_Half = 4U

        /**
         * Processor-specific
         */
        const val ET_LOPROC: Elf_Half = 0xFF00U

        /**
         * Processor-specific
         */
        const val ET_HIPROC: Elf_Half = 0xFFFFU

        /**
         * [e_machine]
         */

        /**
         * AT&T WE 32100
         */
        const val EM_M32: Elf_Half = 1U

        /**
         * SPARC
         */
        const val EM_SPARC: Elf_Half = 2U // SPARC

        /**
         * Intel Architecture
         */
        const val EM_386: Elf_Half = 3U // Intel Architecture

        /**
         * Motorola 68000
         */
        const val EM_68K: Elf_Half = 4U // Motorola 68000

        /**
         * Motorola 88000
         */
        const val EM_88K: Elf_Half = 5U // Motorola 88000

        /**
         * Intel 80860
         */
        const val EM_860: Elf_Half = 7U

        /**
         * MIPS RS3000 Big-Endian
         */
        const val EM_MIPS: Elf_Half = 8U

        /**
         * MIPS RS4000 Big-Endian
         */
        const val EM_MIPS_RS4_BE: Elf_Half = 10U

        /**
         * Fujitsu VPP500
         */
        const val EM_VPP500: Elf_Half = 17U

        /**
         * ARM
         */
        const val EM_ARM: Elf_Half = 40U

        /**
         * X86 64Bit
         */
        const val EM_X86_64: Elf_Half = 62U

        /**
         * RISC-V
         */
        const val EM_RISCV: Elf_Half = 243U

        // ...

        /**
         * [e_machine]
         */

        /**
         * Invalid version
         */
        const val EV_NONE: Elf_Word = 0U

        /**
         * Current version
         */
        const val EV_CURRENT: Elf_Word = 1U
    }

}