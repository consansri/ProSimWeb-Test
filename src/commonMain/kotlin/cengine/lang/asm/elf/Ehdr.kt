package cengine.lang.asm.elf

import cengine.lang.asm.elf.elf32.BinaryProvider
import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr

interface Ehdr<ADDR, OFF> : BinaryProvider {

    val e_ident: E_IDENT
    val e_type: Elf_Half
    val e_machine: Elf_Half
    val e_entry: ADDR
    val e_phoff: OFF
    val e_shoff: OFF
    val e_version: Elf_Word
    val e_flags: Elf_Word
    val e_ehsize: Elf_Half
    val e_phentsize: Elf_Half
    val e_phnum: Elf_Half
    val e_shentsize: Elf_Half
    val e_shnum: Elf_Half
    val e_shstrndx: Elf_Half

    companion object {

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT): Ehdr<*, *> {
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