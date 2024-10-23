package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF32File(name: String, content: ByteArray): ELFFile<ELF32_Ehdr, ELF32_Shdr, ELF32_Phdr, ELF32_Sym, ELF32_Dyn, ELF32_Rel, ELF32_Rela>(name, content){
    constructor(file: VirtualFile): this(file.name, file.getContent())

    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF32_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF32_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF32_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF32_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF32_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF32_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF32_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF32_Rela
}