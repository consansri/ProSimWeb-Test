package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF32File(name: String, content: ByteArray): ELFFile<ELF32_Ehdr, ELF32_Shdr, ELF32_Phdr, ELF32_Sym, ELF32_Dyn, ELF32_Rel, ELF32_Rela>(name, content){
    constructor(file: VirtualFile): this(file.name, file.getContent())
}