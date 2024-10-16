package cengine.lang.asm.elf

import cengine.vfs.VirtualFile

class ELF64File(name: String, content: ByteArray): ELFFile<ELF64_Ehdr, ELF64_Shdr, ELF64_Phdr, ELF64_Sym, ELF64_Dyn, ELF64_Rel, ELF64_Rela>(name, content){
    constructor(file: VirtualFile): this(file.name, file.getContent())
}