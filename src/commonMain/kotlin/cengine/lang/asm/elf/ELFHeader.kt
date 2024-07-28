package cengine.lang.asm.elf


/**
 * ELF Header
 *
 * @param e_ident The initial bytes mark the file as an object file and provide machine-independent data with which to decode and interpret the file's contents.
 *
 */
data class ELFHeader(
    var e_ident: ELF32.UnsignedChar,
    var e_type: ELF32.HALF,
    var e_machine: ELF32.HALF,
    var e_version: ELF32.Word,
    var e_entry: ELF32.ADDR,
    var e_phoff: ELF32.OFF,
    var e_shoff: ELF32.OFF,
    var e_flags: ELF32.Word,
    var e_ehsize: ELF32.HALF,
    var e_phentsize: ELF32.HALF,
    var e_phnum: ELF32.HALF,
    var e_shentsize: ELF32.HALF,
    var e_shnum: ELF32.HALF,
    var e_shstrndx: ELF32.HALF
)
