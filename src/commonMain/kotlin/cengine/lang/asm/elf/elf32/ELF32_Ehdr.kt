package cengine.lang.asm.elf.elf32

import cengine.lang.asm.elf.*
import cengine.lang.asm.elf.Ehdr.Companion.EV_CURRENT


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
    override var e_ident: E_IDENT,
    override var e_type: Elf_Half,
    override var e_machine: Elf_Half,
    override var e_version: Elf_Word = EV_CURRENT,
    override var e_entry: Elf32_Addr,
    override var e_phoff: Elf32_Off,
    override var e_shoff: Elf32_Off,
    override var e_flags: Elf_Word,
    override var e_ehsize: Elf_Half,
    override var e_phentsize: Elf_Half,
    override var e_phnum: Elf_Half,
    override var e_shentsize: Elf_Half,
    override var e_shnum: Elf_Half,
    override var e_shstrndx: Elf_Half
) : Ehdr<Elf32_Addr, Elf32_Off> {

    /*constructor(
        ei_class: Elf_Byte,
        ei_data: Elf_Byte,
        e_type: Elf_Half,

    ): this(
        E_IDENT(ei_class = ei_class, ei_data = ei_data),
        e_type
    )*/


    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }


}
