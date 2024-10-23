package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF64File(name: String, content: ByteArray): ELFFile<ELF64_Ehdr, ELF64_Shdr, ELF64_Phdr, ELF64_Sym, ELF64_Dyn, ELF64_Rel, ELF64_Rela>(name, content){
    constructor(file: VirtualFile): this(file.name, file.getContent())

    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF64_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF64_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF64_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF64_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF64_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF64_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF64_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF64_Rela
}