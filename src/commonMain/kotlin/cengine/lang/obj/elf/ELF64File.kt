package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF64File(name: String, content: ByteArray) : ELFFile<ELF64_Ehdr, ELF64_Shdr, ELF64_Phdr, ELF64_Sym, ELF64_Dyn, ELF64_Rel, ELF64_Rela>(name, content) {
    constructor(file: VirtualFile) : this(file.name, file.getContent())

    override fun getSectionName(section: ELF64_Shdr): String {
        val stringTableOffset = shstrtab?.sh_offset?.toInt() ?: return "[shstrtab missing]"
        val nameOffset = section.sh_name.toInt()
        return content.sliceArray(stringTableOffset + nameOffset until content.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }

    override fun getStrTabString(namendx: Int): String? {
        if (strTab == null) return null
        val strTabOffset = strTab.sh_offset.toInt()
        return content.sliceArray(strTabOffset + namendx until content.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }

    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF64_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF64_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF64_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF64_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF64_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF64_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF64_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF64_Rela

    override fun createSegment(phdr: ELF64_Phdr, sections: List<ELF64_Shdr>): ELF64Segment = ELF64Segment(phdr, sections)
    override fun createSection(shdr: ELF64_Shdr): ELF64Section = ELF64Section(shdr)

    inner class ELF64Section(shdr: ELF64_Shdr) : Section(shdr)
    inner class ELF64Segment(phdr: ELF64_Phdr, sections: List<ELF64_Shdr>) : Segment(phdr, sections)
}