package cengine.lang.obj.elf

sealed interface Rela : BinaryProvider {

    companion object {
        fun size(ei_class: Elf_Byte): Elf_Half {
            return when(ei_class){
                E_IDENT.ELFCLASS32 -> ELF32_Rela.SIZE.toUShort()
                E_IDENT.ELFCLASS64 -> ELF64_Rela.SIZE.toUShort()
                else -> throw ELFGenerator.InvalidElfClassException(ei_class)
            }
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Rela {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val r_offset = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val r_info = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val r_addend = byteArray.loadInt(eIdent, currIndex)
                    return ELF32_Rela(r_offset, r_info, r_addend)
                }

                E_IDENT.ELFCLASS64 -> {
                    val r_offset = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val r_info = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val r_addend = byteArray.loadLong(eIdent, currIndex)
                    return ELF64_Rela(r_offset, r_info, r_addend)
                }

                else -> throw NotInELFFormatException
            }
        }
    }

}