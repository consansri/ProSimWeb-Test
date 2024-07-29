package cengine.lang.asm.elf

/**
 * Character Set: 7-bit ASCII
 */
data class ELF32_Linkable(
    var elf_header: ELF32_Ehdr,
    var phdr: ELF32_Phdr? = null,
    var sections: Array<ELF32_WORD>,
    var shdr: ELF32_Shdr
) {

}