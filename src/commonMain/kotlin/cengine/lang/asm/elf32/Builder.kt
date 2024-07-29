package cengine.lang.asm.elf32

class Builder {

    val elf_header: ELF32_Ehdr = TODO()
    val prog_headers: MutableList<ELF32_Phdr> = mutableListOf()
    val sections: MutableList<ByteArray> = mutableListOf()
    val sec_headers: MutableList<ELF32_Shdr> = mutableListOf()

}