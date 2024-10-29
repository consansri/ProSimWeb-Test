package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF32File(name: String, content: ByteArray) : ELFFile<ELF32_Ehdr, ELF32_Shdr, ELF32_Phdr, ELF32_Sym, ELF32_Dyn, ELF32_Rel, ELF32_Rela>(name, content) {
    constructor(file: VirtualFile) : this(file.name, file.getContent())

    override fun getSectionName(section: ELF32_Shdr): String {
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


    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF32_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF32_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF32_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF32_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF32_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF32_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF32_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF32_Rela


    override fun createSegment(phdr: ELF32_Phdr, sections: List<ELF32_Shdr>): ELF32Segment = ELF32Segment(phdr, sections)

    override fun createSection(shdr: ELF32_Shdr): ELF32Section = ELF32Section(shdr)

    inner class ELF32Section(shdr: ELF32_Shdr) : Section(shdr)
    inner class ELF32Segment(phdr: ELF32_Phdr, sections: List<ELF32_Shdr>) : Segment(phdr, sections)
}